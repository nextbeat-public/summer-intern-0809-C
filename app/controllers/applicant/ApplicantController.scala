/*
 * This file is part of Nextbeat services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package controllers.applicant

import model.component.util.ViewValuePageLayout
import model.site.app.SiteViewValueNewApplicant
import model.site.app.SiteViewValueApplicantShow
import mvc.action.AuthenticationAction
import persistence.applicant.dao.{ApplicantDAO, ApplicantLoginDAO}
import persistence.geo.dao.LocationDAO
import persistence.geo.model.Location
import persistence.applicant.model.Applicant.formForNewApplicant
import persistence.applicant.model.ApplicantLogin
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, MessagesControllerComponents}

import scala.concurrent.Future

// 登録: 新規ユーザー
//~~~~~~~~~~~~~~~~~~~~~
class ApplicantController @javax.inject.Inject()(
val daoLocation:  LocationDAO,
val daoApplicant: ApplicantDAO,
val daoApplicantLogin: ApplicantLoginDAO,
cc: MessagesControllerComponents
) extends AbstractController(cc) with I18nSupport {
  implicit lazy val executionContext = defaultExecutionContext

  val userTypeApplicant = 0
  val userTypeEmployer  = 1

  /**
   * 新規登録ページ
   */
  def newApplicant = Action.async { implicit request =>
    for {
      locSeq <- daoLocation.filterByIds(Location.Region.IS_PREF_ALL)
    } yield {
      val vv = SiteViewValueNewApplicant(
        layout   = ViewValuePageLayout(id = request.uri),
        location = locSeq
      )
      Ok(views.html.site.applicant.form.Main(vv, formForNewApplicant))
    }
  }

  /**
    * 新規登録処理
    */
  def registerApplicant = Action.async { implicit request =>
    formForNewApplicant.bindFromRequest.fold(
      errors => {
        for {
          locSeq <- daoLocation.filterByIds(Location.Region.IS_PREF_ALL)
        } yield {
          val vv = SiteViewValueNewApplicant(
            layout   = ViewValuePageLayout(id = request.uri),
            location = locSeq
          )
          BadRequest(views.html.site.applicant.form.Main(vv, errors))
        }
      },
      applicant   => {
        for {
          aid <- daoApplicant.add(applicant)
          _   <- daoApplicantLogin.update(
            ApplicantLogin(
              aid = Some(aid),
              email = applicant.email,
              password = applicant.password
            )
          )
        } yield {
          // TODO: セッション追加処理
          Redirect(s"/applicant/$aid")
            .withSession(
              request.session + ("aid" -> aid.toString)
            )
        }
      }
    )
  }

  def show(id: Long) = (Action andThen AuthenticationAction(userTypeApplicant)).async { implicit request =>
    val aid = request.session.get("aid")
    aid match {
      case Some(x) => {
        if(x.toLong == id){
          val vv = SiteViewValueApplicantShow(
            layout   = ViewValuePageLayout(id = request.uri),
          )
          Future(Ok(views.html.site.applicant.show.Main(vv)))
        } else {
          Future(Redirect("/", 301))
        }
      }
      case None => Future(Redirect("/", 301))
    }
  }

}

package controllers.applicant_post

import mvc.action.AuthenticationAction
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, MessagesControllerComponents}

import scala.concurrent.Future
// persistence: 永続化
import persistence.applicant_post.model.ApplicantPost.formForApplicantPost
import persistence.applicant_post.dao.ApplicantPostDAO

import persistence.applicant.dao.ApplicantDAO

// model
import persistence.applicant_post.model.ApplicantItem

import persistence.geo.model.Location
import persistence.geo.dao.LocationDAO
import model.site.app.SiteViewValueApplicantPostIndex

import persistence.category.dao.CategoryDAO
import model.site.app.SiteViewValueApplicantPostShow

import model.site.app.SiteViewValueApplicantPost
import model.component.util.ViewValuePageLayout


// 施設
//~~~~~~~~~~~~~~~~~~~~~
class ApplicantPostController @javax.inject.Inject()(
  // val: immutable, var: mutable
  val applicantPostDao: ApplicantPostDAO,
  val applicantDao: ApplicantDAO,
  val daoLocation: LocationDAO,
  val categoryDao: CategoryDAO,
  cc: MessagesControllerComponents
) extends AbstractController(cc) with I18nSupport {
  implicit lazy val executionContext = defaultExecutionContext

  val userTypeApplicant = 0
  val userTypeEmployer  = 1

  def index = Action.async { implicit request =>
    for {
      applicantPost <- applicantPostDao.findAll
      categorys <- categoryDao.findAll
      locations <- daoLocation.filterByIds(Location.Region.IS_PREF_ALL)
    } yield {
      val item = applicantPost.map(p => {
        val location = locations.find(_.id == p.locationId)
        val category1 = categorys.find(_.id.get == p.categoryId1.getOrElse(0))
        val category2 = categorys.find(_.id.get == p.categoryId2.getOrElse(0))
        val category3 = categorys.find(_.id.get == p.categoryId3.getOrElse(0))
        ApplicantItem(
          applicantPost = p,
          locationName = location.map(_.namePref).getOrElse("none"),
          category1 = category1.map(_.name).getOrElse("none"),
          category2 = category2.map(_.name).getOrElse("none"),
          category3 = category3.map(_.name).getOrElse("none")
        )
      })

      val vv = SiteViewValueApplicantPostIndex(
        layout = ViewValuePageLayout(id = request.uri),
        posts = item    
      )
      Ok(views.html.site.applicant_post.index.Main(vv))
    }
  }

  def show(id: Long) = Action.async { implicit request =>
    for {
      applicantPost <- applicantPostDao.get(id)           
      location <- daoLocation.get(applicantPost.get.locationId)
      applicant <- applicantDao.get(applicantPost.get.applicantId)
      categorySeqId = Seq(applicantPost.get.categoryId1, applicantPost.get.categoryId2, applicantPost.get.categoryId3)
      categorys <- categoryDao.filterSeqId(categorySeqId.flatten)
    } yield {
      val vv = SiteViewValueApplicantPostShow(
        layout = ViewValuePageLayout(id = request.uri),
        post = applicantPost.get,
        location = location.get,
        applicant = applicant,
        categorys = categorys
      )

      Ok(views.html.site.applicant_post.show.Main(vv))
    }
  }

  def add = (Action andThen AuthenticationAction(userTypeApplicant)).async { implicit request =>
    val aid = request.session.get("aid")
    aid match {
      case Some(x) => {
        for {
          locSeq <- daoLocation.filterByIds(Location.Region.IS_PREF_ALL)
        } yield {
          val vv = SiteViewValueApplicantPost(
            layout   = ViewValuePageLayout(id = request.uri),
            location = locSeq,
            aid      = x.toLong
          )
          Ok(views.html.site.applicant_post.add.Main(vv, formForApplicantPost))
        }
      }
      case None => {
        Future(Redirect("/", 301))
      }
    }
  }

  def create = (Action andThen AuthenticationAction(userTypeApplicant)).async { implicit request =>
    val aid = request.session.get("aid")
    aid match {
      case Some(x) =>{
        formForApplicantPost.bindFromRequest.fold(
          errors => {
            for {
              locSeq <- daoLocation.filterByIds(Location.Region.IS_PREF_ALL)
            } yield {
              val vv = SiteViewValueApplicantPost(
                layout   = ViewValuePageLayout(id = request.uri),
                location = locSeq,
                aid = x.toLong
              )
              println(errors)
              BadRequest(views.html.site.applicant_post.add.Main(vv, errors))
            }
          },
          form   => {
            for {
              _ <- applicantPostDao.create(form)
            } yield {
              Redirect("/applicant_post")
            }
          }
        )
      }
      case None    =>{
        Future(Redirect("/", 301))
      }
    }
  }

  def edit(id: Long) = TODO

  def update(id: Long) = TODO

  def destroy(id: Long) = TODO
}
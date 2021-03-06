/*
 * This file is part of the Nextbeat services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package persistence.applicant_post.model

import play.api.data._
import play.api.data.Forms._
import java.time.{LocalDate, LocalDateTime, ZoneId}
import java.util.Date

import persistence.geo.model.Location
import persistence.applicant.model.Applicant
import persistence.category.model.Category


// 施設情報 (sample)
//~~~~~~~~~~~~~
case class ApplicantPost(
  id:          Option[ApplicantPost.Id],                // 施設ID
  applicantId: Applicant.Id,
  locationId:  Location.Id,                        // 地域ID
  title:       String,                             // 施設名
  destination: String,                             // 住所(詳細)
  description: String,                             // 施設説明
  categoryId1: Option[Category.Id],
  categoryId2: Option[Category.Id],
  categoryId3: Option[Category.Id],
  done: Boolean,
  free_date: LocalDate,
  updatedAt:   LocalDateTime = LocalDateTime.now,  // データ更新日
  createdAt:   LocalDateTime = LocalDateTime.now   // データ作成日
)

case class ApplicantItem(
  applicantPost: ApplicantPost,
  locationName: String,
  category1: String,
  category2: String,
  category3: String
)

// 施設検索
// case class ApplicantPostSearch(
//   locationIdOpt: Option[Location.Id]
// )


// コンパニオンオブジェクト
//~~~~~~~~~~~~~~~~~~~~~~~~~~
object ApplicantPost {

  // --[ 管理ID ]---------------------------------------------------------------
  type Id = Long

  def date2LocalDate(date: Date): LocalDate =
    return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

  def localDate2Date(localDate: LocalDate): Date =
    return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())


  def applyForm(
    applicantId: Applicant.Id,
    locationId: Location.Id,
    title: String,
    destination: String,
    description: String,
    categoryId1: Option[Category.Id],
    categoryId2: Option[Category.Id],
    categoryId3: Option[Category.Id],
    free_date: Date
  ) = ApplicantPost(
    None, applicantId, locationId, title, destination, description,
    categoryId1, categoryId2, categoryId3, false, date2LocalDate(free_date)
  )

  val formForApplicantPost = Form(
    mapping(
      "applicantId" -> longNumber,
      "locationId"  -> nonEmptyText,
      "title"       -> nonEmptyText,
      "destination" -> nonEmptyText,
      "description" -> text,
      "categoryId1" -> optional(longNumber),
      "categoryId2" -> optional(longNumber),
      "categoryId3" -> optional(longNumber),
      "free_date"   -> date
    )(ApplicantPost.applyForm)(ApplicantPost.unapply(_).map(
      t => (t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, localDate2Date(t._11))
    ))
  )
}

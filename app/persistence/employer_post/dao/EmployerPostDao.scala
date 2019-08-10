package persistence.employer_post.dao

import java.time.LocalDateTime
import java.time.LocalDate
import scala.concurrent.Future

import slick.jdbc.JdbcProfile
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider

import persistence.employer_post.model.EmployerPost
import persistence.geo.model.Location
import persistence.employer.model.Employer
import persistence.category.model.Category

// DAO: 施設情報
//~~~~~~~~~~~~~~~~~~
class EmployerPostDAO @javax.inject.Inject()(
  val dbConfigProvider: DatabaseConfigProvider
) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  // --[ リソース定義 ] --------------------------------------------------------
  lazy val slick = TableQuery[EmployerPostTable]

  // --[ データ処理定義 ] ------------------------------------------------------
  /**
   * 施設を取得
   */
  def get(id: EmployerPost.Id): Future[Option[EmployerPost]] =
    db.run {
      slick
        .filter(_.id === id)
        .result.headOption
    }

  /**
   * 施設を全件取得する
   */
  def findAll: Future[Seq[EmployerPost]] =
    db.run {
      slick.result
    }

  /**
   * 地域から施設を取得
   * 検索業件: ロケーションID
   */
  def filterByLocationIds(locationIds: Seq[Location.Id]): Future[Seq[EmployerPost]] =
    db.run {
      slick
        .filter(_.locationId inSet locationIds)
        .result
    }

  // --[ テーブル定義 ] --------------------------------------------------------
  class EmployerPostTable(tag: Tag) extends Table[EmployerPost](tag, "employer_post") {


    // Table's columns
    def id            = column[EmployerPost.Id]    ("id", O.PrimaryKey, O.AutoInc)
    def employer_id  = column[Employer.Id] ("employer_id")    
    def locationId    = column[Location.Id]    ("location_id")    
    def title         = column[String]         ("title")
    def address   = column[String]         ("address")
    def description   = column[String]         ("description")
    def main_image    = column[String] ("main_image")
    def thumbnail_image = column[String] ("thumbnail_image")
    def price         = column[Int] ("price")
    def category_id_1 = column[Category.Id] ("category_id_1")
    def category_id_2 = column[Category.Id] ("category_id_2")
    def category_id_3 = column[Category.Id] ("category_id_3")
    def done          = column[Boolean] ("done")
    def job_date     = column[LocalDate] ("job_date")
    def updatedAt     = column[LocalDateTime]  ("updated_at")
    def createdAt     = column[LocalDateTime]  ("created_at")

    // The * projection of the table
    def * = (
      id.?, employer_id, locationId, title, address, description, main_image, thumbnail_image, price,
      category_id_1, category_id_2, category_id_3, done, job_date,
      updatedAt, createdAt
    ) <> (
      /** The bidirectional mappings : Tuple(table) => Model */
      (EmployerPost.apply _).tupled,
      /** The bidirectional mappings : Model => Tuple(table) */
      (v: TableElementType) => EmployerPost.unapply(v).map(_.copy(
        _15 = LocalDateTime.now
      ))
    )
  }
}
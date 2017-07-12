package com.jasonmartens

import com.karasiq.bootstrap.Bootstrap.default._
import rx._

import scalatags.JsDom.all._

final class SurnameTable(data: Rx[Seq[TableRow]])(implicit ctx: Ctx.Owner) extends BootstrapComponent {
  override def render(md: Modifier*): Modifier = {
    // Table content
    val heading: Rx[Seq[Modifier]] = Var(
      Seq[Modifier]("id", "Last Name", "Rank", "Count", "Prop 100K", "Cum Prop 100k", "Pct White", "Pct Black",
        "Pct API", "Pct AI & Ala", "Pct 2 Race", "Pct Hisp")
    )

    // Render table
    val pagedTable = PagedTable(heading, data, 10)
    pagedTable.renderTag(TableStyle.bordered, TableStyle.hover, TableStyle.striped, md).render
  }
}
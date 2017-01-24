package com.jasonmartens

import com.karasiq.bootstrap.BootstrapComponent
import com.karasiq.bootstrap.table.{ PagedTable, TableRow, TableStyle }
import rx._

import scalatags.JsDom.all._

final class TestTable(data: Var[Seq[TableRow]])(implicit ctx: Ctx.Owner) extends BootstrapComponent {
  override def render(md: Modifier*): Modifier = {
    // Table content
    val heading = Var(
      Seq[Modifier]("id", "name", "rank", "count", "prop100k", "cum_prop100k", "pctwhite", "pctblack",
        "pctapi", "pctaian", "pct2prace", "pcthispanic")
    )

    val content = data
    // Render table
    val pagedTable = PagedTable(heading, content, 10)
    val renderedTable = pagedTable.renderTag(TableStyle.bordered, TableStyle.hover, TableStyle.striped, md).render

    // Test reactive components
    pagedTable.currentPage.update(2)
    content.update(content.now)
    renderedTable
  }
}
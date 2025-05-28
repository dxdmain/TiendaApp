// app/src/main/java/com/example/tiendaapp/ui/decorations/GridSpacingItemDecoration.kt
package com.example.tiendaapp.ui.decorations

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView


class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacing: Int,
    private val includeEdge: Boolean
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view) // Posición del item
        val column = position % spanCount // Columna del item

        if (includeEdge) {
            // Espaciado para los bordes
            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount

            // Espaciado superior para la primera fila
            if (position < spanCount) outRect.top = spacing

            // Espaciado inferior para todos los items
            outRect.bottom = spacing
        } else {
            // Espaciado sin bordes
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount

            // Espaciado superior excepto para la primera fila
            if (position >= spanCount) outRect.top = spacing
        }
    }
}
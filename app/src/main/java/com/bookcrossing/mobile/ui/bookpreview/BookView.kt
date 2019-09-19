package com.bookcrossing.mobile.ui.bookpreview

import com.bookcrossing.mobile.models.Book
import moxy.MvpView

interface BookView : MvpView {
  fun onBookLoaded(book: Book)

  fun onErrorToLoadBook()

  fun onBookStashed()

  fun onBookUnstashed()

  fun onAbuseReported()
}

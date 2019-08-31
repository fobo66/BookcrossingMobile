package com.bookcrossing.mobile.ui.bookpreview

import com.arellomobile.mvp.MvpView
import com.bookcrossing.mobile.models.Book

interface BookView : MvpView {
  fun onBookLoaded(book: Book)

  fun onErrorToLoadBook()

  fun onBookStashed()

  fun onBookUnstashed()

  fun onAbuseReported()
}

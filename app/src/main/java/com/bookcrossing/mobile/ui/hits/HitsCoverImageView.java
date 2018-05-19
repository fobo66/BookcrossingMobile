package com.bookcrossing.mobile.ui.hits;

import android.content.Context;
import android.util.AttributeSet;
import com.algolia.instantsearch.ui.views.AlgoliaHitView;
import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.modules.GlideApp;
import com.google.firebase.storage.FirebaseStorage;
import org.json.JSONObject;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * (c) 2017 Andrey Mukamolow <fobo66@protonmail.com>
 * Created 23.05.17.
 */

public class HitsCoverImageView extends android.support.v7.widget.AppCompatImageView
    implements AlgoliaHitView {

  public HitsCoverImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  /**
   * @see com.bookcrossing.mobile.presenters.BasePresenter#resolveCover(String)
   */
  @Override public void onUpdateView(JSONObject result) {
    GlideApp.with(this.getContext())
        .load(FirebaseStorage.getInstance().getReference(result.optString("objectID") + ".jpg"))
        .placeholder(R.drawable.ic_book_cover_placeholder).transition(withCrossFade())
        .into(this);
  }
}

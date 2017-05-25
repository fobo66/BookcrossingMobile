package com.bookcrossing.mobile.ui.hits;

import android.content.Context;
import android.util.AttributeSet;

import com.algolia.instantsearch.ui.views.AlgoliaHitView;
import com.bookcrossing.mobile.R;
import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;

import org.json.JSONObject;

/**
 * Created by fobo66 on 23.05.17.
 */

public class HitsCoverImageView extends android.support.v7.widget.AppCompatImageView implements AlgoliaHitView {

    public HitsCoverImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * @see com.bookcrossing.mobile.presenters.BasePresenter#resolveCover(String)
     * */
    @Override
    public void onUpdateView(JSONObject result) {
        Glide.with(this.getContext())
                .using(new FirebaseImageLoader())
                .load(FirebaseStorage.getInstance().getReference(result.optString("objectID") + ".jpg"))
                .placeholder(R.drawable.ic_book_cover_placeholder)
                .crossFade()
                .into(this);
    }
}

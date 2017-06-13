package com.bookcrossing.mobile.util.adapters;

import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.models.Coordinates;
import com.bookcrossing.mobile.ui.bookpreview.BookActivity;

/**
 * (c) 2017 Andrey Mukamolow <fobo66@protonmail.com>
 * Created 10.06.17.
 */

public class PlacesHistoryViewHolder extends BaseViewHolder {

  @BindView(R.id.placesHistoryItem) TextView positionName;

  private Coordinates coordinates;

  public PlacesHistoryViewHolder(View view) {
    super(view);
  }

  public void bind(String positionName, Coordinates coordinates) {
    this.coordinates = coordinates;
    this.positionName.setText(positionName);
  }

  @OnClick(R.id.card) public void goToPlace() {
    ((BookActivity) itemView.getContext()).goToPosition(coordinates);
  }
}

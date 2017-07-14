package com.bookcrossing.mobile.ui.about;

import android.os.Bundle;
import br.tiagohm.markdownview.MarkdownView;
import br.tiagohm.markdownview.css.styles.Github;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.ui.base.BaseActivity;

public class AboutActivity extends BaseActivity {
  @BindView(R.id.markdownView) MarkdownView markdownView;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_about);
    ButterKnife.bind(this);
  }

  @Override protected void onStart() {
    super.onStart();
    markdownView.addStyleSheet(new Github());
    markdownView.loadMarkdownFromAsset("about.md");
  }
}

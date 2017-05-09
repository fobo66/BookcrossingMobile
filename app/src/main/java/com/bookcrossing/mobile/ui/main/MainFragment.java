package com.bookcrossing.mobile.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.models.Book;
import com.bookcrossing.mobile.presenters.MainPresenter;
import com.bookcrossing.mobile.ui.base.BaseFragment;
import com.bookcrossing.mobile.util.adapters.BooksViewHolder;
import com.bookcrossing.mobile.util.listeners.BookListener;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.jakewharton.rxbinding2.view.RxView;

import butterknife.BindView;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class MainFragment extends BaseFragment implements MainView {

    @BindView(R.id.books_rv)
    RecyclerView rv;

    @BindView(R.id.addBookButton)
    FloatingActionButton fab;

    @InjectPresenter
    MainPresenter mainPresenter;

    private BookListener listener;
    private FirebaseRecyclerAdapter<Book, BooksViewHolder> adapter;

    Disposable fabSubscription;

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fabSubscription.dispose();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof BookListener)
        {
            listener = (BookListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement BookListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);
        adapter = new FirebaseRecyclerAdapter<Book, BooksViewHolder>(Book.class, R.layout.book_list_item_main,
                BooksViewHolder.class, mainPresenter.getBooks()) {
            @Override
            protected void populateViewHolder(BooksViewHolder viewHolder, Book model, int position) {
                viewHolder.bind(model);
                viewHolder.setKey(this.getRef(position).getKey());
            }
        };

        rv.setAdapter(adapter);
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(rv);

        fabSubscription = RxView.clicks(fab).subscribe(new Consumer<Object>() {
            @Override
            public void accept(@NonNull Object o) throws Exception {
                listener.onBookAdd();
            }
        });
    }
}

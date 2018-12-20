package com.bozin.worldtraveler.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bozin.worldtraveler.R;
import com.bozin.worldtraveler.model.User;

import java.util.ArrayList;
import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.SearchViewHolder> {

    private ArrayList<User> searchResultList;
    private Context context;

    public SearchResultAdapter(Context context, ArrayList<User> userList) {
        this.context = context;
        searchResultList = userList;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.search_item, parent, false);
        return new SearchViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        String userName = searchResultList.get(position).getUserName();
        holder.searchResultName.setText(userName);
    }

    @Override
    public int getItemCount() {
        return searchResultList.size();
    }

    public void updateUserList(ArrayList<User> userList) {
        searchResultList = userList;
        this.notifyDataSetChanged();
    }


    public class SearchViewHolder extends RecyclerView.ViewHolder {

        private TextView searchResultName;

        public SearchViewHolder(View itemView) {
            super(itemView);
            searchResultName = itemView.findViewById(R.id.tv_search_user_name);
        }
    }
}

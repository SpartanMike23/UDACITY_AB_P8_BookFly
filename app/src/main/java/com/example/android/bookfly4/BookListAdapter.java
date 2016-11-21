package com.example.android.bookfly4;

/**
 * Created by Michael on 9/27/16.
 */
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Michael on 9/26/16.
 */
public class BookListAdapter extends ArrayAdapter <Book> {
    //adapter constructor created for the adapter. Placed ArrayList as one of the parameters
    public BookListAdapter(Context context, ArrayList<Book> books) {

        super(context,0,books);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //set variable for the convertView parameter for access
        View listItemView = convertView;
        //if View is empty, inflate layout list_item
        if(listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item,parent,false);
        }
        //variable created to retrieve position of the current book.
        // Used so each position is populate with its
        //corresponding text
        Book currentBook = getItem(position);

        TextView titleView = (TextView) listItemView.findViewById(R.id.titleView);
        titleView.setText(currentBook.getTitle());

        TextView authorView = (TextView) listItemView.findViewById(R.id.authorView);
        authorView.setText(currentBook.getAuthor());


        return listItemView;
    }
}

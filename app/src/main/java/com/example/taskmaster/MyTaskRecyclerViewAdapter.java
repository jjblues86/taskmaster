package com.example.taskmaster;

import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taskmaster.TaskFragment.OnListFragmentInteractionListener;
import com.example.taskmaster.dummy.DummyContent.DummyItem;

import java.util.List;

import static com.example.taskmaster.R.id.taskName;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyTaskRecyclerViewAdapter extends RecyclerView.Adapter<MyTaskRecyclerViewAdapter.ViewHolder> {

    static final String TAG = "jj.ViewAdapter";
    private final List<Task> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyTaskRecyclerViewAdapter(List<Task> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    //Creates a new row
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_task, parent, false);
        return new ViewHolder(view);
    }

    //Given the holder and the position index, fill in that view with the right data for that position
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.mItem = mValues.get(position);
        holder.mNamedView.setText(mValues.get(position).getTitle());
        holder.mBodyView.setText(mValues.get(position).getBody());
        holder.mStateView.setText(mValues.get(position).getState());
        holder.mImageView.setText(mValues.get(position).getImage());


        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();

            String alltasks = context.getClass().getName();
                System.out.println("alltasks = " + alltasks);

                if(alltasks.equals("com.example.taskmaster.MainActivity")) {
                    Intent intent = new Intent(context, TaskDetailActivity.class);
                    intent.putExtra("mNamedView", holder.mNamedView.getText());
                    intent.putExtra("mBodyView", holder.mBodyView.getText());
                    intent.putExtra("mStateView", holder.mStateView.getText());
                    intent.putExtra("mImageView", holder.mImageView.getText());
                    context.startActivity(intent);
                    TextView textView = (TextView) v.findViewById(taskName);
                } else if(alltasks.equals("com.example.taskmaster.AllTakss")){
                    String title = holder.mNamedView.getText().toString();
                    String body = holder.mBodyView.getText().toString();
                    String state = holder.mStateView.getText().toString();
                    String image = holder.mImageView.getText().toString();

                    Toast toast = Toast.makeText(context, title, Toast.LENGTH_SHORT);
                    toast.show();
                }
//                Log.d("LOGTAG", "clicked : name "+textView.getText().toString() );
               Log.i(TAG, "it was clicked");
            }
        });
    }


    //display how items are in my list
    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mNamedView;
        public final TextView mBodyView;
        public final TextView mStateView;
        public final TextView mImageView;
        public Task mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNamedView = (TextView) view.findViewById(taskName);
            mBodyView = (TextView) view.findViewById(R.id.body);
            mStateView = (TextView) view.findViewById(R.id.state);
            mImageView = (TextView) view.findViewById(R.id.textViewS3);
        }


        @Override
        public String toString() {
            return super.toString() + " '" + mBodyView.getText() + "'";
        }
    }


}

package laioffer.com;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;


public class CommentFragment extends Fragment {

    private GridView mGridView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_comment, container, false);
        mGridView = (GridView) view.findViewById(R.id.comment_grid);
        mGridView.setAdapter(new EventAdapter(getActivity()));
        return view;
    }

    // Change background color if the item is selected
    public void onItemSelected(int position) {
        for (int i = 0; i < mGridView.getChildCount(); i++) {
            if (position == i) {
                mGridView.getChildAt(i).setBackgroundColor(Color.BLUE);
            } else {
                mGridView.getChildAt(i).setBackgroundColor(Color.parseColor("#FAFAFA"));
            }
        }
    }
}
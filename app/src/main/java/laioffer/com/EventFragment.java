package laioffer.com;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EventFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventFragment extends Fragment {

    OnItemSelectListener mCallback;

    public EventFragment() {
    }

    public static EventFragment newInstance() {
        EventFragment fragment = new EventFragment();
        Bundle bundle = new Bundle();
        bundle.putString("name", "test");
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event, container, false);
        ListView listView = (ListView) view.findViewById(R.id.event_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_list_item_1, getEventNames());
        // Assign adapter to ListView.
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mCallback.onItemSelected(i); }
            });
        return view;
    }

    private String[] getEventNames() {
        String[] names = {
                "Event1", "Event2", "Event3", "Event4", "Event5", "Event6", "Event7", "Event8", "Event9", "Event10", "Event11", "Event12"};
        return names;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnItemSelectListener) context;
        } catch (ClassCastException e) {
            //do something
        }
    }

    // Container Activity must implement this interface
    public interface OnItemSelectListener {
        public void onItemSelected(int position);
    }

}
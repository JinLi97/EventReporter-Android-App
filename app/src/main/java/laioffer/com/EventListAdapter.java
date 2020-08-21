package laioffer.com;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.AsyncTask;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.NativeContentAd;
import com.google.android.gms.ads.formats.NativeContentAdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Adler32;

public class EventListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Event> eventList;
    private DatabaseReference databaseReference;
    private Context context;
    private static final String ADMOB_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110";
    private static final String ADMOB_APP_ID = "ca-app-pub-3940256099942544/3347511713";

    //TYPE_ITEM = event; TYPE_ADS = ads
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_ADS = 1;

    private AdLoader.Builder builder;
    private LayoutInflater inflater;

    //keep position of the ads in the list
    private Map<Integer, Object> map = new HashMap<>();
    

    public EventListAdapter(List<Event> events, final Context context) {
        this.context = context;
        eventList = events;
        databaseReference = FirebaseDatabase.getInstance().getReference();
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //set ads to corresponding position
        eventList = new ArrayList<Event>();
        int count = 0;
        for (int i = 0; i < events.size(); i++) {
            if (i % 2 == 1) {
                map.put(i + count, new Object());
                count++;
                eventList.add(new Event());
            }
            eventList.add(events.get(i));
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v;
        switch (viewType) {
            case TYPE_ITEM:
                v = inflater.inflate(R.layout.event_list_item, parent, false);
                viewHolder = new ViewHolder(v);
                break;
            case TYPE_ADS:
                v = inflater.inflate(R.layout.ad_container_layout, parent, false);
                viewHolder = new ViewHolderAds(v);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        switch (holder.getItemViewType()) {
            case TYPE_ITEM:
                ViewHolder viewHolderItem = (ViewHolder) holder;
                configureItemView(viewHolderItem, position);
                break;
            case TYPE_ADS:
                ViewHolderAds viewHolderAds = (ViewHolderAds) holder;
                refreshAd(viewHolderAds.frameLayout);
                break;
        }
    }


    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView location;
        public TextView description;
        public TextView time;
        public ImageView imgview;
        public ImageView img_view_good;
        public ImageView img_view_comment;
        public TextView good_number;
        public TextView comment_number;

        public View layout;

        public ViewHolder(View v) {
            super(v);
            layout = v;
            title = (TextView)v.findViewById(R.id.event_item_title);
            location = (TextView)v.findViewById(R.id.event_item_location);
            description = (TextView)v.findViewById(R.id.event_item_description);
            time = (TextView)v.findViewById(R.id.event_item_time);
            imgview = (ImageView)v.findViewById(R.id.event_item_img);
            img_view_good = (ImageView)v.findViewById(R.id.event_good_img);
            img_view_comment = (ImageView)v.findViewById(R.id.event_comment_img);
            good_number = (TextView)v.findViewById(R.id.event_good_number);
            comment_number = (TextView)v.findViewById(R.id.event_comment_number);
        }
    }

    //View Holder Class for Ads
    public class ViewHolderAds extends RecyclerView.ViewHolder {
        public FrameLayout frameLayout;
        public ViewHolderAds(View v) {
            super(v);
            frameLayout = (FrameLayout)v;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return map.containsKey(position) ? TYPE_ADS : TYPE_ITEM;
    }

    //show Event
    private void configureItemView(final ViewHolder holder, final int position) {
        final Event event = eventList.get(position);
        holder.title.setText(event.getTitle());
        String[] locations = event.getAddress().split(",");
        holder.location.setText(locations[1] + "," + locations[2]);
        holder.description.setText(event.getDescription());
        holder.time.setText(Utils.timeTransformer(event.getTime()));
        holder.good_number.setText(String.valueOf(event.getLike()));
        //set comment number
        if (event.getImgUri() != null) {
            final String url = event.getImgUri();
            holder.imgview.setVisibility(View.VISIBLE);
            new AsyncTask<Void, Void, Bitmap>(){
                @Override
                protected Bitmap doInBackground(Void... voids) {
                    return Utils.getBitmapFromURL(url);
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    holder.imgview.setImageBitmap(bitmap);
                }
            }.execute();
        }else {
            holder.imgview.setVisibility(View.GONE);
        }

        //When user likes the event,
        holder.img_view_good.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                databaseReference.child("events").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Event recordedevent = snapshot.getValue(Event.class);
                            if (recordedevent.getId().equals(event.getId())) {
                                int number = recordedevent.getLike();
                                holder.good_number.setText(String.valueOf(number + 1));
                                snapshot.getRef().child("like").setValue(number + 1);
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        holder.layout.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CommentActivity.class);
                String eventId = event.getId();
                intent.putExtra("EventID", eventId);
                context.startActivity(intent);
            }
        });
    }

    //refresh ads, first load ads from remote, second, add content to ads view
    private void refreshAd(final FrameLayout frameLayout) {
        AdLoader.Builder builder = new AdLoader.Builder(context, ADMOB_AD_UNIT_ID);
        builder.forContentAd(new NativeContentAd.OnContentAdLoadedListener() {
            @Override
            public void onContentAdLoaded(NativeContentAd nativeContentAd) {
                NativeContentAdView adView = (NativeContentAdView) inflater.inflate(R.layout.ads_contain, null);
                populateContentAdView(nativeContentAd, adView);
                frameLayout.removeAllViews();
                frameLayout.addView(adView);
            }
        });

        AdLoader adLoader = builder.withAdListener(new AdListener(){
            @Override
            public void onAdFailedToLoad(int errorCode) {

            }
        }).build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void populateContentAdView(NativeContentAd nativeContentAd, NativeContentAdView adView) {
        adView.setHeadlineView(adView.findViewById(R.id.ads_headline));
        adView.setImageView(adView.findViewById(R.id.ads_image));
        adView.setBodyView(adView.findViewById(R.id.ads_body));
        adView.setAdvertiserView(adView.findViewById(R.id.ads_advertiser));

        //some assets are guaranteed to be in every NativeContentAd
        ((TextView)adView.getHeadlineView()).setText(nativeContentAd.getHeadline());
        ((TextView)adView.getBodyView()).setText(nativeContentAd.getBody());
        ((TextView)adView.getAdvertiserView()).setText(nativeContentAd.getAdvertiser());

        List<NativeAd.Image> imgaes = nativeContentAd.getImages();

        if (imgaes.size() > 0) {
            ((ImageView)adView.getImageView()).setImageDrawable(imgaes.get(0).getDrawable());
        }

        //assign native ad object to the native view
        adView.setNativeAd(nativeContentAd);
    }

}

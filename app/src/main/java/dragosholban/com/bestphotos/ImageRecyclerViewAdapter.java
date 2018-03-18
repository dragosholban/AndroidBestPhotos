package dragosholban.com.bestphotos;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ImageRecyclerViewAdapter extends RecyclerView.Adapter {
    private static final int TYPE_SQUARE = 0;
    private static final int TYPE_VERTICAL = 1;

    public static class SquareImageViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView likesTextView;

        public SquareImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.gridImageview);
            likesTextView = itemView.findViewById(R.id.likesCount);
        }
    }

    public static class VerticalImagesViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView1;
        ImageView imageView2;
        TextView likesTextView1;
        TextView likesTextView2;

        public VerticalImagesViewHolder(View itemView) {
            super(itemView);
            imageView1 = itemView.findViewById(R.id.gridImageview1);
            likesTextView1 = itemView.findViewById(R.id.likesCount1);
            imageView2 = itemView.findViewById(R.id.gridImageview2);
            likesTextView2 = itemView.findViewById(R.id.likesCount2);
        }
    }

    private static class MyImage {
        FacebookImage image;
        int span = 1;

        public MyImage(FacebookImage image, int span) {
            this.image = image;
            this.span = span;
        }
    }

    private Context mContext;
    private ArrayList<ArrayList<MyImage>> images = new ArrayList<>();

    public ImageRecyclerViewAdapter(Context context, ArrayList<FacebookImage> images) {
        this.mContext = context;
        distributeImages(images);
    }

    private void distributeImages(ArrayList<FacebookImage> images) {
        ArrayList<MyImage> arrayOfImages = new ArrayList<>();
        int i = 0;
        int rightPos = 0;
        boolean left = true;
        int span;
        for (FacebookImage image : images) {
            i++;
            span = 1;
            if (left) {
                if (i % 6 == 1) {
                    span = 2;
                    rightPos = i + 8;
                }
                arrayOfImages.add(new MyImage(image, span));
                if (i % 6 == 2) {
                    left = false;
                    continue;
                }
            } else {
                if (i % rightPos == 0) {
                    span = 2;
                    left = true;
                }
                arrayOfImages.add(new MyImage(image, span));
                if (i % rightPos == rightPos - 2) {
                    continue;
                }
            }
            this.images.add(arrayOfImages);
            arrayOfImages = new ArrayList<>();
        }

        if(arrayOfImages.size() > 0) {
            this.images.add(arrayOfImages);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        GridLayoutManager layoutManager = new GridLayoutManager(mContext, 3);

        // Create a custom SpanSizeLookup where the first item spans both columns
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return images.get(position).get(0).span;
            }
        });

        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_VERTICAL) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.vertical_square_images, parent, false);
            VerticalImagesViewHolder holder = new VerticalImagesViewHolder(view);

            return holder;
        } else {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.square_image, parent, false);
            SquareImageViewHolder holder = new SquareImageViewHolder(view);

            return holder;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ArrayList<MyImage> imgs = images.get(position);
        if (imgs.size() > 1) {
            VerticalImagesViewHolder myHolder = (VerticalImagesViewHolder) holder;
            Picasso.get().load(imgs.get(0).image.url).into(myHolder.imageView1);
            myHolder.likesTextView1.setText(String.valueOf(imgs.get(0).image.reactions));
            myHolder.imageView1.setTag(imgs.get(0).image.link);

            Picasso.get().load(imgs.get(1).image.url).into(myHolder.imageView2);
            myHolder.likesTextView2.setText(String.valueOf(imgs.get(1).image.reactions));
            myHolder.imageView2.setTag(imgs.get(1).image.link);
        } else {
            SquareImageViewHolder myHolder = (SquareImageViewHolder) holder;
            Picasso.get().load(imgs.get(0).image.url).into(myHolder.imageView);
            myHolder.likesTextView.setText(String.valueOf(imgs.get(0).image.reactions));
            myHolder.imageView.setTag(imgs.get(0).image.link);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (images.get(position).size() > 1) {
            return TYPE_VERTICAL;
        }

        return TYPE_SQUARE;
    }

    @Override
    public int getItemCount() {
        return images.size();
    }
}

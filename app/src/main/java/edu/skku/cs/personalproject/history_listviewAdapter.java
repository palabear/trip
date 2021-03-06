package edu.skku.cs.personalproject;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class history_listviewAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<history> histories;
    private String filedir;
    private final String accessKey = BuildConfig.ACCESSKEY;
    private final String secretKey = BuildConfig.SECRETKEY;
    private static final String bucketName = "zappa-7lelfnbhz";

    public history_listviewAdapter(Context mContext, ArrayList<history> histories, String filedir) {
        this.mContext = mContext;
        this.histories = histories;
        this.filedir = filedir;
    }

    @Override
    public int getCount() {
        return histories.size();
    }

    @Override
    public Object getItem(int i) {
        return histories.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.history_sublayout, viewGroup, false);

        ImageView temp_pic = view.findViewById(R.id.picture);
        TextView show_location = view.findViewById(R.id.location);
        show_location.setText(histories.get(i).location);

        try {

            AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
            AmazonS3Client s3Client = new AmazonS3Client(awsCredentials, Region.getRegion(Regions.AP_NORTHEAST_2));
            TransferUtility transferUtility = TransferUtility.builder().s3Client(s3Client).context(mContext).build();
            TransferNetworkLossHandler.getInstance(mContext);

            File picture = File.createTempFile("JPEG_", ".jpg");

            TransferObserver downloadObserver = transferUtility.download(bucketName,
                    filedir + histories.get(i).pic_name, picture);



            downloadObserver.setTransferListener(new TransferListener() {
                @Override
                public void onStateChanged(int id, TransferState state) {
                    if (state == TransferState.COMPLETED) {
                        Bitmap mbp = BitmapFactory.decodeFile(picture.getAbsolutePath().trim());
                        Uri pic_uri = FileProvider.getUriForFile(mContext,"edu.skku.cs.personalproject.fileprovider",picture);
                        temp_pic.setImageURI(pic_uri);
                    }


                }

                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                }

                @Override
                public void onError(int id, Exception ex) {
                    Log.e(TAG, ex.getMessage());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }




        return view;
    }
}
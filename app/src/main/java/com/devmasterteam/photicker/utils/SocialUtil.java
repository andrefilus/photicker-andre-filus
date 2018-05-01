package com.devmasterteam.photicker.utils;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.devmasterteam.photicker.R;
import com.devmasterteam.photicker.views.MainActivity;
import com.devmasterteam.utils.ImageUtil;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Usuario on 01/05/2018.
 */

public class SocialUtil {

    private static final String HASHTAG = "#photoTickerApp";

    public static void shareImageOnInsta(MainActivity mainActivity, RelativeLayout mRelativePhotoContent, View view) {
        PackageManager pkManager = mainActivity.getPackageManager();
        try {
            pkManager.getPackageInfo("com.instagram.android", 0);

            try{
                Bitmap image = ImageUtil.drawBitmap(mRelativePhotoContent);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

                File file = new File(Environment.getExternalStorageDirectory() + File.separator + "temp_file.jpg");

                try {
                    file.createNewFile();
                    FileOutputStream fo = new FileOutputStream(file);
                    fo.write(bytes.toByteArray());

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/temp_file.jpg"));
                    sendIntent.setType("image/*");
                    sendIntent.setPackage("com.instagram.android");

                    view.getContext().startActivity(Intent.createChooser(sendIntent, mainActivity.getString(R.string.share_image)));
                } catch (FileNotFoundException e) {
                    Toast.makeText(mainActivity, R.string.unexpected_error, Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(mainActivity, R.string.unexpected_error, Toast.LENGTH_SHORT).show();
                }

            }catch (Exception ex){
                Toast.makeText(mainActivity, R.string.unexpected_error, Toast.LENGTH_SHORT).show();
            }
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(mainActivity, R.string.instagram_not_installed, Toast.LENGTH_SHORT).show();
        }
    }

    public static void shareImageOnWhats(MainActivity mainActivity, RelativeLayout mRelativePhotoContent, View view) {
        PackageManager pkManager = mainActivity.getPackageManager();
        try {
            pkManager.getPackageInfo("com.whatsapp", 0);

            String fileName = "temp_file" + System.currentTimeMillis() + ".jpg";

            try {
                mRelativePhotoContent.setDrawingCacheEnabled(true);
                mRelativePhotoContent.buildDrawingCache(true);
                File imageFile = new File(Environment.getExternalStorageDirectory(), fileName);
                FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
                //100 é o mínimo de perda possível de qualidade
                mRelativePhotoContent.getDrawingCache(true).compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.close();
                mRelativePhotoContent.setDrawingCacheEnabled(false);
                //libera recursos
                mRelativePhotoContent.destroyDrawingCache();

                try {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, HASHTAG);
                    sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/" + fileName));

                    sendIntent.setType("image/jpeg");
                    sendIntent.setPackage("com.whatsapp");

                    //compartilha no whatsapp
                    view.getContext().startActivity(Intent.createChooser(sendIntent, mainActivity.getString(R.string.share_image)));
                } catch (Exception e) {
                    Toast.makeText(mainActivity, R.string.unexpected_error, Toast.LENGTH_SHORT).show();
                }
            } catch (FileNotFoundException e) {
                Toast.makeText(mainActivity, R.string.unexpected_error, Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(mainActivity, R.string.unexpected_error, Toast.LENGTH_SHORT).show();
            }
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(mainActivity, R.string.whatsapp_not_installed, Toast.LENGTH_SHORT).show();
        }
    }

    public static void shareImageOnTwitter(MainActivity mainActivity, RelativeLayout mRelativePhotoContent, View view) {
        PackageManager pkManager = mainActivity.getPackageManager();
        try {
            pkManager.getPackageInfo("com.twitter.android", 0);
            String fileName = "temp_file" + System.currentTimeMillis() + ".jpg";

            try {
                Intent tweetIntent = new Intent(Intent.ACTION_SEND);
                Bitmap image = ImageUtil.drawBitmap(mRelativePhotoContent);

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

                File file = new File(Environment.getExternalStorageDirectory() + File.separator + "temp_file.jpg");
                file.createNewFile();
                FileOutputStream fo = new FileOutputStream(file);
                fo.write(bytes.toByteArray());

                tweetIntent.putExtra(Intent.EXTRA_TEXT, HASHTAG);
                tweetIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/temp_file.jpg"));
                tweetIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile
                        (mainActivity,"com.devmasterteam.photicker.fileprovider" , file));
                tweetIntent.setType("image/jpeg");
//                tweetIntent.setType("image/*");
                //qual pacote pode tratar a intent
                PackageManager pm = mainActivity.getPackageManager();
                List<ResolveInfo> resolveInfoList = pm.queryIntentActivities(tweetIntent, PackageManager.MATCH_DEFAULT_ONLY);
                boolean resolved = false;
                for (ResolveInfo ri : resolveInfoList) {
                    if (ri.activityInfo.name.contains("twitter")) {
                        tweetIntent.setClassName(ri.activityInfo.packageName, ri.activityInfo.name);
                        resolved = true;
                        break;
                    }
                }
                view.getContext().startActivity(resolved ? tweetIntent : Intent.createChooser(tweetIntent, mainActivity.getString(R.string.share_image)));
            } catch (FileNotFoundException e) {
                Toast.makeText(mainActivity, R.string.unexpected_error, Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(mainActivity, R.string.unexpected_error, Toast.LENGTH_SHORT).show();
            }

        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(mainActivity, R.string.twitter_not_installed, Toast.LENGTH_SHORT).show();
        }
    }

    public static void shareImageOnFace(MainActivity mainActivity, RelativeLayout mRelativePhotoContent, View view) {
//        PackageManager packageManager = mainActivity.getPackageManager();

        SharePhoto photo = new SharePhoto.Builder().setBitmap(ImageUtil.drawBitmap(mRelativePhotoContent)).build();

        SharePhotoContent content = new SharePhotoContent.Builder().addPhoto(photo)
                .setShareHashtag(new ShareHashtag.Builder().setHashtag(HASHTAG).build()).build();

        new ShareDialog(mainActivity).show(content);
    }
}

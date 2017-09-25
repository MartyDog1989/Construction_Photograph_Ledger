package jp.martydog.construction_photograph_ledger;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTextArray;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    public final static String EXTRA_PIC = "jp.martydog.construction_photograph_ledger.PICTURE";
    public static final String LOG_TAG = "testPDF";
    private Realm realm;
    private RealmChangeListener realmChangeListener = new RealmChangeListener() {
        @Override
        public void onChange(Object element) {
            reloadListView();
        }
    };
    private ListView listView;
    private ListAdapter listAdapter;

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている

            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {

        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                startActivity(intent);
            }
        });


        realm = Realm.getDefaultInstance();
        realm.addChangeListener(realmChangeListener);

        listAdapter = new ListAdapter(MainActivity.this);
        listView = (ListView) findViewById(R.id.listView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Picture picture = (Picture) parent.getAdapter().getItem(position);
                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                intent.putExtra(EXTRA_PIC, picture.getId());
                startActivity(intent);
            }
        });

        // ListViewを長押ししたときの処理
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                // タスクを削除する

                final Picture p = (Picture) parent.getAdapter().getItem(position);

                // ダイアログを表示する
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("削除");
                builder.setMessage("削除しますか");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        RealmResults<Picture> results = realm.where(Picture.class).equalTo("id", p.getId()).findAll();

                        realm.beginTransaction();
                        results.deleteAllFromRealm();
                        realm.commitTransaction();

                        reloadListView();
                    }
                });
                builder.setNegativeButton("CANCEL", null);
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
        });

        reloadListView();
    }

    private void reloadListView() {
        RealmResults<Picture> pictureRealmResults = realm.where(Picture.class).findAllSorted("id", Sort.ASCENDING);
        listAdapter.setPictureList(realm.copyFromRealm(pictureRealmResults));
        listView.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
    }

    private void createPdf() throws IOException, DocumentException {

        RealmResults<Picture> realmResults = realm.where(Picture.class).findAllSorted("id", Sort.ASCENDING);

        File pdfFolder = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "pdfdemo");
        if (!pdfFolder.exists()) {
            pdfFolder.mkdir();
            Log.d(LOG_TAG, "Pdf Directory created");
        }

        //Create time stamp
        Date date = new Date();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(date);

        File myFile = new File(pdfFolder + "/" + timeStamp + ".pdf");
        Log.d(LOG_TAG, myFile.toString());

        OutputStream output = new FileOutputStream(myFile);

        //Step 1
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);

        //Step 2
        PdfWriter pdfWriter = PdfWriter.getInstance(document, output);

        //Step 3
        document.open();

        //日本語にいけるように追加する。
        // フォントの作成
        BaseFont baseFont = BaseFont.createFont(
                "HeiseiMin-W3",       // 平成明朝体
                "UniJIS-UCS2-HW-H",   // 横書き指定&英数を半角幅で印字
                BaseFont.NOT_EMBEDDED);

        BaseFont baseFont1 = BaseFont.createFont(
                "HeiseiKakuGo-W5",
                "UniJIS-UCS2-H",
                BaseFont.NOT_EMBEDDED);

        Font font18 = new Font(baseFont, 18);

        float imageMargin = 222.5f;
        float textMargin = 30;
        float increase1 = imageMargin;//増加値
        int margin = 35;
        int loop = 0;

        //Step 4 Add content
        for (Picture picture : realmResults) {
            //テキストの表示位置を変更する
            PdfContentByte pdfContentByte = pdfWriter.getDirectContent();

            pdfContentByte.beginText();
            pdfContentByte.setFontAndSize(baseFont1, 18);
            pdfContentByte.setTextMatrix(400, document.getPageSize().getHeight() - (textMargin + margin));

            if (picture.getText().toString().contains("\n")) {
                String[] arr = picture.getText().toString().split("\n");
                int increase2 = 20;
                for (String str : arr) {
                    pdfContentByte.showText(str);
                    pdfContentByte.setTextMatrix(400, document.getPageSize().getHeight() - (textMargin + margin + increase2));
                    increase2 += 20;
                }
            } else {
                pdfContentByte.showText(new PdfTextArray(picture.getText().toString()));

            }
            pdfContentByte.endText();

            Image image = Image.getInstance(picture.getBitmapArray());
            image.setAbsolutePosition(30, document.getPageSize().getHeight() - (imageMargin + margin));
            image.scaleAbsolute(317.5f, 222.5f);
            pdfContentByte.addImage(image);

            loop++;

            if (loop % 3 == 0) {
                imageMargin = 222.5f;
                textMargin = 30;
                margin = 35;
                document.newPage();
            } else {
                imageMargin += increase1;
                textMargin += increase1;
                margin += 50;
            }

        }


        //Step 5: Close the document
        document.close();
        pdfWriter.close();

        String fileDir = myFile.toString();

        new AlertDialog.Builder(this)
                .setTitle("PDF出力完了")
                .setMessage(fileDir + "\nに出力しました。")
                .setPositiveButton("OK", null)
                .show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            try {
                createPdf();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (DocumentException e) {
                e.printStackTrace();
            }
        } else {
            realm.beginTransaction();
            realm.deleteAll();
            realm.commitTransaction();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
                break;
            default:
                break;
        }
    }
}

package com.futureworkshops.android.autocapture.camera.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;

import com.futureworkshops.android.autocapture.R;
import com.futureworkshops.android.autocapture.camera.MainCameraPresenter;
import com.futureworkshops.datacap.common.camera.configuration.Configuration;
import com.futureworkshops.datacap.common.model.Batch;
import com.futureworkshops.datacap.common.model.Document;
import com.futureworkshops.datacap.common.model.Field;
import com.futureworkshops.datacap.common.model.FieldChar;
import com.futureworkshops.datacap.common.model.Page;
import com.futureworkshops.datacap.common.model.Property;
import com.ibm.datacap.sdk.api.DatacapApi;
import com.ibm.datacap.sdk.common.DatacapImageProcessor;
import com.ibm.datacap.sdk.model.IDocumentType;
import com.ibm.datacap.sdk.model.IFieldType;
import com.ibm.datacap.sdk.model.IPageType;
import com.ibm.datacap.sdk.model.IProperty;
import com.ibm.datacap.sdk.ui.image.DocumentCornerPickerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.futureworkshops.android.autocapture.camera.MainCameraPresenter.IMAGE;
import static com.futureworkshops.android.autocapture.camera.view.MainActivity.ARGS_BITMAP_PATH;
import static com.futureworkshops.android.autocapture.camera.view.MainActivity.ARGS_CORNERS;
import static com.futureworkshops.datacap.common.camera.configuration.ConfigurationService.batchConfiguratorHelper;

public class CropActivity extends AppCompatActivity {

    public static final String PROPERTY_TYPE = "TYPE";
    public static final String PROPERTY_STATUS = "STATUS";
    public static final String PROPERTY_IMAGE_FILE = "IMAGEFILE";
    public static final String PROPERTY_LABEL = "label";

    private static final String CROPPED_IMAGE = "cropped_image.png";

    private IDocumentType documentType;

    private ImageView imageView;
    private DocumentCornerPickerView documentCornerPickerView;
    private DatacapImageProcessor datacapImageProcessor;
    private View deskew;
    private View upload;

    @Inject
    DatacapApi datacapApi;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        datacapImageProcessor = new DatacapImageProcessor(this);

        imageView = findViewById(R.id.bitmap_preview);
        documentCornerPickerView = findViewById(R.id.corner_picker);

        final Parcelable[] parcelables = getIntent().getExtras().getParcelableArray(ARGS_CORNERS);

        imageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                final String previewPath = getIntent().getExtras().getString(ARGS_BITMAP_PATH);
                Bitmap bitmap = from(previewPath, imageView.getWidth(), imageView.getHeight());

                if (parcelables == null) {
                    Point[] coordinates = new Point[4];
                    coordinates[0] = new Point(imageView.getLeft() + 50, imageView.getTop() + 50);
                    coordinates[1] = new Point(imageView.getRight() - 50, imageView.getTop() + 50);
                    coordinates[2] = new Point(imageView.getLeft() + 50, imageView.getBottom() - 50);
                    coordinates[3] = new Point(imageView.getRight() - 50, imageView.getBottom() - 50);

                    documentCornerPickerView.setInitialCornerPosition(coordinates);

                } else {
                    Point[] corners = new Point[parcelables.length];
                    for (int i = 0; i < parcelables.length; i++) {
                        PointF pointF = (PointF) parcelables[i];
                        corners[i] = new Point((int) pointF.x, (int) pointF.y);
                    }

                    imageView.setImageBitmap(bitmap);
                    documentCornerPickerView.setInitialCornerPosition(
                            getScaledInitialDeskewPoints(bitmap.getWidth(), bitmap.getHeight(),
                                    previewPath + "/" + IMAGE, corners));
                }


                return true;
            }
        });

        upload = findViewById(R.id.upload);
        deskew = findViewById(R.id.deskew);
        deskew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                List<Point> pointList = documentCornerPickerView.getSelectedCorners();
                Point[] corners = new Point[pointList.size()];
                for (int i = 0; i < pointList.size(); i++) {
                    corners[i] = pointList.get(i);
                }

                datacapImageProcessor.applyPerspectiveCorrection(bitmap, corners).subscribe(new Observer<Bitmap>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
                        Log.e("Error", "bitmap deskewed");
                        deskew.setVisibility(View.GONE);
                        documentCornerPickerView.setVisibility(View.GONE);
                        imageView.setImageBitmap(bitmap);
                        final String pathToUpload = MainCameraPresenter.saveToInternalStorage(bitmap, CropActivity.this, CROPPED_IMAGE);
                        final File imageToUpload = new File(pathToUpload, CROPPED_IMAGE);

                        upload.setVisibility(View.VISIBLE);
                        upload.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Batch batch = createBatch(imageToUpload);
                                datacapApi.uploadBatch(Configuration.APPLICATION_TYPE, "Mobile only", batch, "")
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Observer<Void>() {
                                            @Override
                                            public void onCompleted() {

                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                Log.e("Error", "Unable to upload batch", e);
                                                uploadSuccessful();
                                            }

                                            @Override
                                            public void onNext(Void aVoid) {
                                                Log.e("Error", "Batch upload successful");
                                                uploadSuccessful();
                                            }
                                        });
                            }
                        });
                    }
                });
            }
        });
    }

    private void uploadSuccessful() {
        Toast.makeText(this, "Upload successful", Toast.LENGTH_LONG).show();
        finish();
    }

    private Bitmap from(String path, int width, int height) {
        File image = new File(path, IMAGE);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);

        return Bitmap.createScaledBitmap(bitmap, width, height, false);
    }

    private Point[] getScaledInitialDeskewPoints(int width, int height, String path, Point[] corners) {
        BitmapFactory.Options characteristics = getImageCharacteristics(path);
        int originalWidth = characteristics.outWidth;
        int originalHeight = characteristics.outHeight;

        float xScale = width * 1.0f / originalWidth;
        float yScale = height * 1.0f / originalHeight;

        return mapPageCorners(xScale, yScale, corners);
    }

    private BitmapFactory.Options getImageCharacteristics(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        //Returns null, sizes are in the options variable
        BitmapFactory.decodeFile(path, options);
        return options;
    }

    private Point[] mapPageCorners(float xScale, float yScale, Point[] corners) {
        final Point[] points = new Point[4];

        for (int i = 0; i < corners.length; i++) {
            Point corner = corners[i];
            int x = (int) (xScale * corner.x);
            int y = (int) (yScale * corner.y);
            points[i] = new Point(x, y);
        }

        return points;
    }

    public Batch createBatch(File image) {
        Batch batch = new Batch();
        batch.setId(String.valueOf(System.currentTimeMillis()));
        String batchType = batchConfiguratorHelper.getBatchType().getType();

        List<Property> properties = new ArrayList<>();
        properties.add(new Property(PROPERTY_TYPE, batchType));
        properties.add(new Property(PROPERTY_STATUS, "0"));

        batch.setProperties(properties);

        final Document document = createDocument();

        //  set page to document
        List<Page> pages = new ArrayList<>();
        pages.add(createPage(image));
        document.setPages(pages);

        // set documents to batch
        List<Document> documents = new ArrayList<>();
        documents.add(document);
        batch.setDocuments(documents);

        return batch;
    }

    private Document createDocument() {
        Document document = new Document();
        document.setId(String.valueOf(System.currentTimeMillis()));

        // add default properties
        documentType = batchConfiguratorHelper.getAllowedDocumentTypes().get(0);
        List<Property> properties = new ArrayList<>();
        for (IProperty property : documentType.getProperties()) {

            if (property.getName().equals(PROPERTY_TYPE)) {
                properties.add(new Property(PROPERTY_TYPE, documentType.getType()));
            } else {
                properties.add(new Property(property.getName(), property.getValue()));
            }
        }
        document.setProperties(properties);

        return document;
    }

    private Page createPage(File image) {
        //  get page types
        final List<IPageType> pageTypesForDocument = batchConfiguratorHelper
                .getPageTypesForDocument(documentType);

        // get field types for the selected page type
        IPageType defaultPageType = pageTypesForDocument.get(0);
        List<IFieldType> pageFields = batchConfiguratorHelper.getPageFields(defaultPageType);

        final Page page = new Page();
        page.setId(String.valueOf(System.currentTimeMillis()));

        // add default properties
        List<Property> properties = new ArrayList<>();
        for (IProperty property : defaultPageType.getProperties()) {
            if (property.getName().equals(PROPERTY_TYPE)) {
                properties.add(new Property(PROPERTY_TYPE, defaultPageType.getType()));
            } else if (property.getName().equalsIgnoreCase(PROPERTY_IMAGE_FILE)) {
                properties.add(new Property(PROPERTY_IMAGE_FILE, image.getPath()));
            } else {
                properties.add(new Property(property.getName(), property.getValue()));
            }
        }

        page.setProperties(properties);


        List<Field> fields = new ArrayList<>();
        for (IFieldType fieldType : pageFields) {
            fields.add(createField(fieldType));
        }

        page.setFields(fields);

        // save for later

        return page;
    }

    private Field createField(IFieldType fieldType) {
        // this is called from within a transaction
        Field field = new Field();
        field.setId(String.valueOf(System.nanoTime()));

        // some fields don't have the 'label' property
        boolean hasLabel = false;
        List<Property> fieldProps = new ArrayList<>();
        for (IProperty property : fieldType.getProperties()) {
            if (property.getName().equalsIgnoreCase(PROPERTY_TYPE)) {
                fieldProps.add(new Property(PROPERTY_TYPE, fieldType.getType()));
            } else {
                hasLabel = property.getName().equalsIgnoreCase(PROPERTY_LABEL);

                fieldProps.add(new Property(property.getName(), property.getValue()));
            }
        }

        // if we don't have a label we need to use the field type
        if (!hasLabel) {
            fieldProps.add(
                    new Property(PROPERTY_LABEL, fieldType.getType()));
        }

        field.setProperties(fieldProps);
        field.setValue("ajsdkhasjd");
        updatePageField(field, field.getLabel(), field.getValue());

        return field;
    }

    private void updatePageField(Field field, String fieldName, String fieldvalue) {
        if (field.getType().equalsIgnoreCase(fieldName)) {
            field.setValue(fieldvalue);
            field.setFieldChars(createFieldChars(fieldvalue));
        }
    }

    private List<FieldChar> createFieldChars(@NonNull String fieldvalue) {
        List<FieldChar> charList = new ArrayList<>();
        final char[] chars = fieldvalue.toCharArray();
        for (char c : chars) {
            charList.add(new FieldChar(getAsciiRepresentation(c)));
        }

        return charList;
    }

    private String getAsciiRepresentation(char character) {
        int ascii = (int) character;
        return String.valueOf(ascii);
    }
}

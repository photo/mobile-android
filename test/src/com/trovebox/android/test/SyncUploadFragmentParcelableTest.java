
package com.trovebox.android.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.os.Parcel;
import android.test.InstrumentationTestCase;

import com.trovebox.android.app.SyncImageSelectionFragment.CustomImageWorkerAdapter;
import com.trovebox.android.app.SyncImageSelectionFragment.ImageData;
import com.trovebox.android.app.SyncImageSelectionFragment.SelectionController;

public class SyncUploadFragmentParcelableTest extends InstrumentationTestCase {
    public void testImageData()
    {
        String data = "data";
        long id = 1;
        ImageData imageData = new ImageData(id, data);
        validateImageData(imageData, id, data);

        Parcel parcel = Parcel.obtain();
        imageData.writeToParcel(parcel, 0);
        // done writing, now reset parcel for reading
        parcel.setDataPosition(0);
        // finish round trip
        ImageData createFromParcel = ImageData.CREATOR.createFromParcel(parcel);

        validateImageData(createFromParcel, id, data);
    }

    public void validateImageData(ImageData imageData, long id, String data)
    {
        assertNotNull(imageData);
        assertEquals(imageData.id, id);
        assertEquals(imageData.data, data);
    }

    public void testSelectionController()
    {
        SelectionController selectionController = new SelectionController();
        long[] selectedIds = new long[] {
                1, 2, 3, 4, 8, 9, Long.MAX_VALUE - 1, Long.MAX_VALUE
        };
        long[] notSelectedIds = new long[] {
                5, 6, 7, 11, 22, Long.MAX_VALUE - 2
        };
        for (long id : selectedIds)
        {
            selectionController.addToSelected(id);
        }
        validateSelectionController(selectionController, selectedIds, notSelectedIds);

        Parcel parcel = Parcel.obtain();
        selectionController.writeToParcel(parcel, 0);
        // done writing, now reset parcel for reading
        parcel.setDataPosition(0);
        // finish round trip
        selectionController = SelectionController.CREATOR.createFromParcel(parcel);

        validateSelectionController(selectionController, selectedIds, notSelectedIds);
    }

    public void validateSelectionController(SelectionController selectionController,
            long[] selectedIds,
            long[] notSelectedIds
            )
    {
        assertNotNull(selectionController);
        if (selectedIds != null)
        {
            for (long id : selectedIds)
            {
                assertTrue(selectionController.isSelected(id));
            }
        }
        if (notSelectedIds != null)
        {
            for (long id : notSelectedIds)
            {
                assertFalse(selectionController.isSelected(id));
            }
        }
    }

    public void testCustomImageWorkerAdapter()
    {
        long[] imageDataIds = new long[] {
                1, 2, 3, 4
        };
        String[] imageDataData = new String[] {
                "a", "b", "c", "d"
        };
        String[] processedValuesArray = new String[] {
                "b", "c"
        };
        int[] filteredIndexesArray = new int[] {
                2, 3
        };
        boolean filtered = true;
        testCustomImageWorkerAdapter(imageDataIds, imageDataData, processedValuesArray,
                filteredIndexesArray, filtered);
        testCustomImageWorkerAdapter(imageDataIds, imageDataData, processedValuesArray,
                null, filtered);
    }

    public void testCustomImageWorkerAdapter(long[] imageDataIds, String[] imageDataData,
            String[] processedValuesArray, int[] filteredIndexesArray, boolean filtered) {
        List<ImageData> all = new ArrayList<ImageData>();
        for (int i = 0; i < imageDataIds.length; i++)
        {
            long id = imageDataIds[i];
            String data = imageDataData[i];
            ImageData imageData = new ImageData(id, data);
            validateImageData(imageData, id, data);
            all.add(imageData);
        }
        Set<String> processedValues = new TreeSet<String>(Arrays.asList(processedValuesArray));

        List<Integer> filteredIndexes = null;
        if (filteredIndexesArray != null)
        {
            filteredIndexes = new ArrayList<Integer>();
            for (int i = 0; i < filteredIndexesArray.length; i++)
            {
                filteredIndexes.add(filteredIndexesArray[i]);
            }
        }


        CustomImageWorkerAdapter adapter = new CustomImageWorkerAdapter(all, processedValues,
                filteredIndexes,
                filtered);
        validateCustomImageWorkerAdapter(adapter, imageDataIds, imageDataData,
                processedValuesArray, filteredIndexesArray, filtered);

        Parcel parcel = Parcel.obtain();
        adapter.writeToParcel(parcel, 0);
        // done writing, now reset parcel for reading
        parcel.setDataPosition(0);
        // finish round trip
        adapter = CustomImageWorkerAdapter.CREATOR.createFromParcel(parcel);

        validateCustomImageWorkerAdapter(adapter, imageDataIds, imageDataData,
                processedValuesArray, filteredIndexesArray, filtered);
    }

    public void validateCustomImageWorkerAdapter(CustomImageWorkerAdapter adapter,
            long[] imageDataIds, String[] imageDataData,
            String[] processedValuesArray,
            int[] filteredIndexesArray,
            boolean filtered)
    {
        List<ImageData> all = adapter.all;
        assertNotNull(all);
        assertEquals(all.size(), imageDataIds.length);
        for (int i = 0; i < imageDataIds.length; i++)
        {
            long id = imageDataIds[i];
            String data = imageDataData[i];
            ImageData imageData = all.get(i);
            validateImageData(imageData, id, data);
        }

        Set<String> processedValues = adapter.processedValues;
        assertNotNull(processedValues);
        assertEquals(processedValues.size(), processedValuesArray.length);
        for (int i = 0; i < processedValuesArray.length; i++)
        {
            String value = processedValuesArray[i];
            assertTrue(processedValues.contains(value));
        }

        if (filteredIndexesArray != null)
        {
            List<Integer> filteredIndexes = adapter.filteredIndexes;
            assertNotNull(filteredIndexes);
            assertEquals(filteredIndexes.size(), filteredIndexesArray.length);
            for (int i = 0; i < filteredIndexesArray.length; i++)
            {
                Integer ix = filteredIndexesArray[i];
                assertEquals(ix, filteredIndexes.get(i));
            }
        } else
        {
            assertNull(adapter.filteredIndexes);
        }
        assertEquals(filtered, adapter.filtered);
    }

}

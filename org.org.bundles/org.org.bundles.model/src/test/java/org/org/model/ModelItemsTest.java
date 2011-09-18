package org.org.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

/**
 * @author pagregoire
 */
public class ModelItemsTest extends TestCase {
    private static List<ModelItemEvent> EVENT_MONITOR = new ArrayList<ModelItemEvent>();

    public void testRootModelItem() {
        final String rootModelItem_UID = "rootModelItem_UID";
        final String itemLevel1_1UID = "itemLevel1_1UID";
        final String itemLevel1_2UID = "itemLevel1_2UID";
        final String itemLevel2_1UID = "itemLevel2_1UID";
        // final String itemLevel2_2UID = "itemLevel2_2UID";
        PhonyModelItemLevel1 itemLevel1_1 = new PhonyModelItemLevel1(itemLevel1_1UID);
        PhonyModelItemLevel1 itemLevel1_2 = new PhonyModelItemLevel1(itemLevel1_2UID);
        // TEST SINGLETON AND ROOT ITEM BEHAVIOUR
        assertFalse(RootModelItem.isInstanciated(rootModelItem_UID));
        RootModelItem.getInstance(rootModelItem_UID);
        assertTrue(RootModelItem.isInstanciated(rootModelItem_UID));
        assertTrue(RootModelItem.getInstance(rootModelItem_UID) instanceof RootModelItem);
        assertTrue(RootModelItem.getInstance(rootModelItem_UID) instanceof IModelItem);
        RootModelItem<PhonyModelItemLevel1> rootModelItem = RootModelItem.<PhonyModelItemLevel1>getInstance(rootModelItem_UID);
        assertNull(rootModelItem.getParent());
        try {
            rootModelItem.setParent(itemLevel1_1);
            fail("This should throw a ModelException as you should never set a Parent to the root model item");
        } catch (ModelException me) {
        }
        // TEST UID
        assertEquals(rootModelItem_UID, rootModelItem.getUID());
        // TEST EMPTY STATUS
        assertFalse(rootModelItem.hasChildren());
        // TEST CHILDS' ADDING
        rootModelItem.addChild(itemLevel1_1);
        rootModelItem.addChild(itemLevel1_2);
        assertTrue(rootModelItem.hasChildren());
        assertTrue(rootModelItem.hasChild(itemLevel1_1UID));
        assertFalse(rootModelItem.hasChild(itemLevel2_1UID));
        // TEST CHILDREN GETTERS
        assertEquals(itemLevel1_1, rootModelItem.getChild(itemLevel1_1UID));
        Set<PhonyModelItemLevel1> children = rootModelItem.getChildren();
        assertEquals(2, children.size());
        for (Iterator<?> it = rootModelItem.getChildren().iterator(); it.hasNext();) {
            Object next = it.next();
            assertTrue(next instanceof IModelItem);
            assertTrue(next.equals(itemLevel1_1) || next.equals(itemLevel1_2));
        }
        // TEST CHILDS' REMOVING
        rootModelItem.removeChild(itemLevel1_1UID);
        assertFalse(rootModelItem.hasChild(itemLevel1_1UID));
        assertTrue(rootModelItem.hasChildren());
        // TEST TOSTRING() BROWSING
        assertTrue("RootModelItem should have an UID starting with:"+rootModelItem_UID,rootModelItem.toString().startsWith(rootModelItem_UID));
        // TEST CHILDS' COMPLETE CLEARING
        rootModelItem.clearChildren();
        assertFalse(rootModelItem.hasChildren());
        assertEquals(rootModelItem_UID, rootModelItem.toString());
        class ModelItemListener implements IModelItemListener {
            /**
             * @see org.org.model.IModelItemListener#changeOccured(ModelItemEvent)
             */
            public void changeOccured(ModelItemEvent modelItemEvent) {
                ModelItemsTest.EVENT_MONITOR.add(modelItemEvent);
            }
        }
        ModelItemListener modelItemListener = new ModelItemListener();
        rootModelItem.addListener(modelItemListener);
        assertEquals(0,ModelItemsTest.EVENT_MONITOR.size());
        rootModelItem.addChild(itemLevel1_1);
        assertEquals(2,ModelItemsTest.EVENT_MONITOR.size());

        ModelItemEvent testEvent = (ModelItemEvent) ModelItemsTest.EVENT_MONITOR.get(0);
        assertEquals(ModelItemEvent.EventType.PRE_ADD_CHILD, testEvent.getEventType());
        assertEquals(rootModelItem, testEvent.getSourceItem());
        assertEquals(itemLevel1_1, testEvent.getTargetItem());

        testEvent = (ModelItemEvent) ModelItemsTest.EVENT_MONITOR.get(1);
        assertEquals(ModelItemEvent.EventType.POST_ADD_CHILD, testEvent.getEventType());
        assertEquals(rootModelItem, testEvent.getSourceItem());
        assertEquals(itemLevel1_1, testEvent.getTargetItem());

        ModelItemsTest.EVENT_MONITOR.clear();
        
        modelItemListener = new ModelItemListener();
        assertEquals(0,ModelItemsTest.EVENT_MONITOR.size());
        rootModelItem.addChild(itemLevel1_1);
        assertEquals(2,ModelItemsTest.EVENT_MONITOR.size());

        testEvent = (ModelItemEvent) ModelItemsTest.EVENT_MONITOR.get(0);
        assertEquals(ModelItemEvent.EventType.PRE_UPDATE_CHILD, testEvent.getEventType());
        assertEquals(rootModelItem, testEvent.getSourceItem());
        assertEquals(itemLevel1_1, testEvent.getTargetItem());


        testEvent = (ModelItemEvent) ModelItemsTest.EVENT_MONITOR.get(1);
        assertEquals(ModelItemEvent.EventType.POST_UPDATE_CHILD, testEvent.getEventType());
        assertEquals(rootModelItem, testEvent.getSourceItem());
        assertEquals(itemLevel1_1, testEvent.getTargetItem());
        
        ModelItemsTest.EVENT_MONITOR.clear();

        rootModelItem.removeChild(itemLevel1_1UID);
        int size = ModelItemsTest.EVENT_MONITOR.size();
        assertTrue(size == 2);

        testEvent = (ModelItemEvent) ModelItemsTest.EVENT_MONITOR.get(0);
        assertEquals(ModelItemEvent.EventType.PRE_REMOVE_CHILD, testEvent.getEventType());
        assertEquals(rootModelItem, testEvent.getSourceItem());
        assertEquals(itemLevel1_1, testEvent.getTargetItem());

        testEvent = (ModelItemEvent) ModelItemsTest.EVENT_MONITOR.get(1);
        assertEquals(ModelItemEvent.EventType.POST_REMOVE_CHILD, testEvent.getEventType());
        assertEquals(rootModelItem, testEvent.getSourceItem());
        assertEquals(itemLevel1_1, testEvent.getTargetItem());

        ModelItemsTest.EVENT_MONITOR.clear();

        rootModelItem.toggleListenersOff();

        rootModelItem.addChild(itemLevel1_1);

        assertEquals(0,ModelItemsTest.EVENT_MONITOR.size());

        rootModelItem.toggleListenersOn();

        rootModelItem.addChild(itemLevel1_2);

        assertEquals(2,ModelItemsTest.EVENT_MONITOR.size());

        ModelItemsTest.EVENT_MONITOR.clear();

        rootModelItem.removeListener(modelItemListener);

        rootModelItem.removeChild(itemLevel1_1UID);

        assertEquals(1,rootModelItem.getChildren().size());

    }

    public void testAbstractModelItem() {
        final String rootModelItem_UID = "rootModelItem_UID2";
        final String itemLevel1_1UID = "itemLevel1_1UID";
        final String itemLevel1_2UID = "itemLevel1_2UID";
        final String itemLevel2_1UID = "itemLevel2_1UID";
        final String itemLevel2_2UID = "itemLevel2_2UID";
        PhonyModelItemLevel1 itemLevel1_1 = new PhonyModelItemLevel1(itemLevel1_1UID);
        PhonyModelItemLevel1 itemLevel1_2 = new PhonyModelItemLevel1(itemLevel1_2UID);
        PhonyModelItemLevel2 itemLevel2_1 = new PhonyModelItemLevel2(itemLevel2_1UID);
        PhonyModelItemLevel2 itemLevel2_2 = new PhonyModelItemLevel2(itemLevel2_2UID);
        // TEST UID
        assertEquals(itemLevel1_1UID, itemLevel1_1.getUID());
        // TEST EMPTY STATUS
        assertFalse(itemLevel1_1.hasChildren());
        // TEST CHILDS' ADDING
        itemLevel1_1.addChild(itemLevel2_1);
        itemLevel1_1.addChild(itemLevel2_2);
        assertTrue(itemLevel1_1.hasChildren());
        assertTrue(itemLevel1_1.hasChild(itemLevel2_1UID));
        assertFalse(itemLevel1_1.hasChild(itemLevel1_2UID));
        // TEST CHILDREN GETTERS
        assertEquals(itemLevel2_1, itemLevel1_1.getChild(itemLevel2_1UID));
        Set<PhonyModelItemLevel2> children = itemLevel1_1.getChildren();
        assertEquals(2, children.size());
        for (Iterator<?> it = itemLevel1_1.getChildren().iterator(); it.hasNext();) {
            Object next = it.next();
            assertTrue(next instanceof IModelItem);
            assertTrue(next.equals(itemLevel2_1) || next.equals(itemLevel2_2));
        }
        // TEST CHILDS' REMOVING
        assertTrue(itemLevel1_1.hasChild(itemLevel2_2UID));
        itemLevel1_1.removeChild(itemLevel2_2UID);
        assertFalse(itemLevel1_1.hasChild(itemLevel2_2UID));
        // TEST PARENT SETTER/GETTER
        itemLevel1_1.setParent(RootModelItem.getInstance(rootModelItem_UID));
        assertEquals(RootModelItem.getInstance(rootModelItem_UID), itemLevel1_1.getParent());
        // TEST CHILDS' COMPLETE CLEARING
        itemLevel1_1.clearChildren();
        assertFalse(itemLevel1_1.hasChildren());
        // TEST TOSTRING() BROWSING
        assertEquals("[\nUID=" + itemLevel1_1UID + ";\n]", itemLevel1_1.toString());
        itemLevel1_1.addChild(itemLevel2_1);
        assertEquals("[\nUID=" + itemLevel1_1UID + ";\n\tUID=" + itemLevel2_1UID + ";\n]", itemLevel1_1.toString());
        assertFalse(itemLevel1_2.hasChild(itemLevel2_2UID));
        itemLevel2_2.setParent(itemLevel1_2);
        assertEquals(itemLevel1_2, itemLevel2_2.getParent());
        assertTrue(itemLevel1_2.hasChild(itemLevel2_2UID));
        assertTrue(itemLevel1_1.compareTo(itemLevel1_1) == 0);
        assertTrue(itemLevel1_2.compareTo(itemLevel1_1) > 0);
        assertTrue(itemLevel1_1.compareTo(itemLevel1_2) < 0);
    }
}
package io.collective;

import java.time.Clock;


public class SimpleAgedCache {
    //Member variables for SimpleAgedCache
    private Object[] keyArray;
    private Object[] valueArray;
    private int maxSize;
    private int currSize;
    private Clock myCacheClock;
    private ExpirableEntry expiryObject = new ExpirableEntry();

    public SimpleAgedCache(Clock clock) {
        this.maxSize = 0;
        this.currSize = 0;
        this.keyArray = new Object[this.maxSize];
        this.valueArray = new Object[this.maxSize];
        this.myCacheClock = clock;
    }

    public SimpleAgedCache() {
        this.maxSize = 0;
        this.currSize = 0;
        this.keyArray = new Object[this.maxSize];
        this.valueArray = new Object[this.maxSize];
        this.myCacheClock = Clock.systemDefaultZone();  //set to this instead of null
        // since there will be a null pointer exception in the program otherwise
    }

    //inner class can access private members of outer class
    //therefore we do not need to pass a SimpleAgedCache instance
    public class ExpirableEntry {

        public void pruneExpiredEntries() {
            //Remove any expired entries
            int originalCurrSize = currSize;
            int numberExpired = 0;

            for (int i = 0; i < originalCurrSize; i++) {
                String[] parseItems = valueArray[i].toString().split("-");
                long expireTime = Long.parseLong(parseItems[1]);
                if (expireTime < myCacheClock.millis()) {
                    numberExpired++;
                    //mark the entries we want to remove with null
                    keyArray[i] = null;
                    valueArray[i] = null;
                }
            }

            convertArrayForContiguity(originalCurrSize, numberExpired);

        }

        //convert arrays into contiguous arrays without any null entry
        public void convertArrayForContiguity(int originalCurrSize, int numberExpired) {
            //originalCurrSize is the size of the arrays before pruning began
            if (originalCurrSize == numberExpired) {
                currSize = 0;
                maxSize = 0;
                keyArray = new Object[maxSize];
                valueArray = new Object[maxSize];
            } else {
                Object[] newKeyArray = new Object[2 * originalCurrSize];
                Object[] newValueArray = new Object[2 * originalCurrSize];
                int counter = 0;

                for (int i = 0; i < originalCurrSize; i++) {
                    if (keyArray[i] != null && valueArray[i] != null) {
                        newKeyArray[counter] = keyArray[i];
                        newValueArray[counter] = valueArray[i];
                        counter++;
                    }

                }
                keyArray = newKeyArray;
                valueArray = newValueArray;
                maxSize = 2 * originalCurrSize;
                currSize = counter;
            }
        }
    }

    public void put(Object key, Object value, int retentionInMillis) {
        //if the key already exists, do nothing
        for (int i = 0; i < currSize; i++) {
            if (this.keyArray[i].equals(key)) {
                return;
            }
        }

        try {
            this.expiryObject.pruneExpiredEntries();
        } catch (NullPointerException e) {
            System.out.println(("Null pointer exception!"));
        }

        //Expand if no space to put
        if (this.maxSize == 0) {
            this.maxSize++;
            this.keyArray = new Object[this.maxSize];
            this.valueArray = new Object[this.maxSize];

        } else if (this.maxSize == this.keyArray.length) {

            //Initialize the tempArray to be double the maxSize
            Object[] newKeyArray = new Object[2 * this.maxSize];
            Object[] newValueArray = new Object[2 * this.maxSize];

            //Copy over the data
            for (int i = 0; i < this.maxSize; i++) {
                newKeyArray[i] = this.keyArray[i];
                newValueArray[i] = this.valueArray[i];
            }

            //double this.maxSize and replace arrays
            this.maxSize = 2 * this.maxSize;
            this.keyArray = newKeyArray;
            this.valueArray = newValueArray;
        }

        //Now that we should have space to put, perform the put operation
        this.keyArray[this.currSize] = key;

        long expireTime = retentionInMillis + this.myCacheClock.millis();
        String valueWithExpireTime = value.toString() + "-" + expireTime;
        this.valueArray[this.currSize] = valueWithExpireTime;
        this.currSize++;

    }

    public boolean isEmpty() {
        boolean result = false;

        if (this.currSize <= 0) {
            result = true;
        }

        return result;
    }

    public int size() {
        this.expiryObject.pruneExpiredEntries();
        return this.currSize;
    }

    public Object get(Object key) {
        // Parse out the first entry of parseItems[] array which contains the value
        // Second entry is expireTime in String format
        try {
            this.expiryObject.pruneExpiredEntries();
        } catch (NullPointerException e) {
            System.out.println(("Null pointer exception!"));
        }
        Object resultValue = null;
        for (int i = 0; i < this.currSize; i++) {
            if (this.keyArray[i].equals(key)) {
                String[] parseItems = this.valueArray[i].toString().split("-");
                resultValue = parseItems[0];
            }
        }

        return resultValue;
    }
}
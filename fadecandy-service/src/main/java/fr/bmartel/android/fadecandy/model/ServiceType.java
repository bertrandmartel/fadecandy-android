/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2016-2018 Bertrand Martel
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.bmartel.android.fadecandy.model;

/**
 * Service Type : persistent or non persistent.
 *
 * @author Bertrand Martel
 */
public enum ServiceType {

    PERSISTENT_SERVICE(0),
    NON_PERSISTENT_SERVICE(1);

    private int mState;

    private ServiceType(int state) {
        mState = state;
    }

    public static int getState(ServiceType type) {
        switch (type) {
            case PERSISTENT_SERVICE:
                return 0;
            case NON_PERSISTENT_SERVICE:
                return 1;
        }
        return 0;
    }

    public static ServiceType getServiceType(int serviceType) {
        switch (serviceType) {
            case 0:
                return PERSISTENT_SERVICE;
            case 1:
                return NON_PERSISTENT_SERVICE;
        }
        return PERSISTENT_SERVICE;
    }
}

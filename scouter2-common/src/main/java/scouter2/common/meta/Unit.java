/*
 * Copyright 2019. The Scouter2 Authors.
 *
 *  @https://github.com/scouter-project/scouter2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package scouter2.common.meta;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-03
 */
public class Unit {
    String display = "";
    UnitFormat format = UnitFormat.REAL;
    int decimalPlace = 2;

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public UnitFormat getFormat() {
        return format;
    }

    public boolean isInteger() {
        return format == UnitFormat.INTEGER;
    }

    public void setFormat(UnitFormat format) {
        this.format = format;
    }

    public int getDecimalPlace() {
        return decimalPlace;
    }

    public void setDecimalPlace(int decimalPlace) {
        this.decimalPlace = decimalPlace;
    }

    @Override
    public String toString() {
        return "Unit{" +
                "display='" + display + '\'' +
                ", format=" + format +
                ", decimalPlace=" + decimalPlace +
                '}';
    }
}

# Calendar View Library

A Month and a Week view group to present data.

Features:
* Customizable colors and text sizes
* Overflow mark below the day when the views don't fit
* First day of the week can be set to Sunday, Saturday or Monday

### Screenshots
<img src="http://i.imgur.com/ux0thSQ.jpg" width="250px"> <img src="http://i.imgur.com/PhSw78x.jpg" width="500px">

### Add to your project using gradle
```groovy
compile 'com.sickmartian.calendarview:calendarview:1.0.0'
```

### Customizable properties at a glance
```xml
  <com.sickmartian.calendarview.MonthView
      xmlns:calendar_view="http://schemas.android.com/apk/res-auto"
      android:layout_below="@+id/control_container"
      calendar_view:textSize="12sp"
      calendar_view:activeTextColor="@color/colorPrimaryText"
      calendar_view:inactiveTextColor="@color/colorSecondaryText"
      calendar_view:activeBackgroundColor="@color/colorCalendarDayBackground"
      calendar_view:selectedBackgroundColor="@color/selectedDayBackground"
      calendar_view:inactiveBackgroundColor="@color/notThisMonthDayBackground"
      calendar_view:currentDayDecorationDrawable="@drawable/current_day_drawable"
      calendar_view:currentDayDecorationSize="24dp"
      calendar_view:currentDayTextColor="@color/colorInvertedText"
      calendar_view:showOverflow="false"
      calendar_view:overflowColor="@color/colorPrimary"
      calendar_view:overflowHeight="2dp"
      calendar_view:separatorColor="@color/colorCalendarDivider"
      android:id="@+id/monthView"
      android:layout_width="match_parent"
      android:layout_height="match_parent"/>
```

### API

The API for getting the pressed, current and/or selected day works via the **DayMetadata** class. `DayMetadata` is just a value holder for the day, month and year. It takes the months of the year starting with 1 (so January is 1, December is 12, like joda-time does)

Some methods also have a `Calendar` alternative that is _just there for convenience_, in this case the Day, Month and Year values will be read directly, no timezone awarenes is built in.

Adding views to the view group can be done via via the day of the month for the `MonthView` (`addViewToDayInCurrentMonth`) or using DayMetadata for the `WeekView` (`addViewToDay`).

Alternatively you can use the cell id (`addViewToCell`), this is not perfect as the state won't be preserved on rotation in some cases but at least allows you to add data to neightbor months when using `MonthView`.

First day of the week is set like this
```java
  mCalendarView.setFirstDayOfTheWeek(CalendarView.SUNDAY_SHIFT);
```

### In Action
This library powers the [Trackendar App that you can find on Google Play](https://play.google.com/store/apps/details?id=com.sickmartian.calendartracker) download it to see it in a production scenario

Alternatively just clone the repo and submodule using

`git clone --recursive https://github.com/sickmartian/CalendarView.git`

The sample app that's on the screenshots can be used to test the library.

###License
<pre>
Copyright 2017 sickmartian

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
</pre>

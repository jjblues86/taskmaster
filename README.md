# taskmaster

## Description
- This is an Android App which allows users to add tasks and can also view all their added tasks for future reference.


## Screenshot

![screenshot](https://github.com/jjblues86/taskmaster/blob/master/screenshots/Screen%20Shot%201398-11-24%20at%2012.32.51%20PM.png)

## Technology
- Android Studio

## Daily Log Change
- Add a Task : enables a user to add a task
- All Tasks : displays all the tasks that a user added
- Settings Page : A button on the main page to when users click on it should send them to the settings page.
- Task Detail Page : This page should render the details of a title task clicked from the home page.
- Homepage : The main page should be modified to contain three different buttons with hardcoded task titles. When a user taps one of the titles, it should go to the Task Detail page, and the title at the top of the page should match the task title that was tapped on the previous page.
             The homepage should also contain a button to visit the Settings page, and once the user has entered their username, it should display “{username}’s tasks” above the three task buttons.
- Task Model
- Create a Task class. A Task should have a title, a body, and a state. The state should be one of “new”, “assigned”, “in progress”, or “complete”.
- Create a ViewAdapter class that displays data from a list of Tasks.
In your MainActivity, create at least three hardcoded Task instances and use those to populate your RecyclerView/ViewAdapter.

## To Run
- Clone this repo
- run gradle build
- Initialize android studio

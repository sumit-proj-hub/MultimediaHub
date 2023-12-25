# MultimediaHub

A very simple android application to list and view image, video, audio and PDF files.

## Features
- **Showing Files**: Application can show files in both list and grid view based on selected file type and sorting method.
- **Show By Folder**: Application can show collection of media files by grouping them on the basis of directory in which media files are located. User can see a list/grid of folders and tapping on that folder will show all media files within that folder.
- **Recents**: Application also shows the collection of recently opened media files using this application sorted on the basis of last opened time.
- **Search Bar**: User can search media files from its name.
- **Image Viewer**: Very basic image viewer that supports pinching and panning actions in image.
- **Audio Player**: Very basic audio player that shows seek bar showing current audio progress, time elapsed and total audio time. It has play/pause button, fast-forward and rewind button. Playback can be controlled using headphones. Currently audio is played in an activity instead of a foreground service. It does not support audio control from notification. This behaviour is intended as it is not a music player application. This application is only intended to just play/view different types of media files.
- **Video Player**: Basic video player that has basic controls: play/pause, seek, show time elapsed and total video time, audio track selection, fast forward, fast rewind and orientation change. Its playback can also be controlled using headphones.
- **PDF Viewer**: Extremely basic PDF viewer that renders PDF file. It supports pinching, panning, fast scroll, showing current page number and jumping to given page number. It also supports night mode.

  ## Future Developments
  Currently the application does not support opening media files using "Open with..." dialog of android from different applications. Maybe it will be supported in future versions.

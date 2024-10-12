import com.raylib.Jaylib;
import com.raylib.Raylib;
import java.lang.Math;

import java.io.File;
import java.util.Arrays;

import static com.raylib.Jaylib.*;

class Main{

    public static void main(String[] args) {
        final int NUM_FRAMES = 3;
        int screenWidth = 800;
        int screenHeight = 600;
        Raylib.Color sliderColor;
        InitWindow(screenWidth, screenHeight, "Music Player");

        InitAudioDevice();

        Font AfacadMediumFont = LoadFont("assets/fonts/afacad-flux/static/AfacadFlux-Medium.ttf");

        Texture hoverPlayBtn = LoadTexture("assets/sprites/play.png");
        Texture hoverPauseBtn = LoadTexture("assets/sprites/pause.png");
        Texture playBtn = LoadTexture("assets/sprites/play-grey.png");
        Texture pauseBtn = LoadTexture("assets/sprites/pause-grey.png");
        Texture musicCover = LoadTexture("assets/sprites/music-maroon.png");
        Texture hoverMediaStepForwardBtn = LoadTexture("assets/sprites/media-step-forward.png");
        Texture hoverMediaStepBackwardBtn = LoadTexture("assets/sprites/media-step-backward.png");
        Texture greyMediaStepForwardBtn = LoadTexture("assets/sprites/media-step-forward-grey.png");
        Texture greyMediaStepBackwardBtn = LoadTexture("assets/sprites/media-step-backward-grey.png");

        String songsFolderPath = "music";
        File musicFolder = new File(songsFolderPath);
        File[] songs = musicFolder.listFiles();
        assert songs != null;
        String[] songNames = new String[songs.length];
        for (int i = 0; i < songs.length; i++) {
            songNames[i] = songs[i].getName();

        }

        int currentSong = (int)songNames.length/2;
        String musicName = songNames[currentSong];
        Music music = LoadMusicStream(songsFolderPath + "/" + musicName);
        Jaylib.Rectangle musicSliderRec = new Jaylib.Rectangle(100, 450, 400, 462);
        Jaylib.Rectangle musicSliderBounds = new Jaylib.Rectangle(musicSliderRec);
        musicSliderBounds.width(musicSliderBounds.width() - musicSliderBounds.x());
        musicSliderBounds.height(musicSliderBounds.height() - musicSliderBounds.y());

        Texture PlayPauseBtn = pauseBtn;
        Jaylib.Rectangle PlayPauseRec = new Jaylib.Rectangle(0, 0, (float) PlayPauseBtn.width(), (float) PlayPauseBtn.height());
        Jaylib.Rectangle btnBounds = new Jaylib.Rectangle((musicSliderRec.width() + musicSliderRec.x()) / 2 - PlayPauseBtn.width() / 2.0f, screenHeight - 100 - PlayPauseBtn.height() / NUM_FRAMES / 2.0f, (float) PlayPauseBtn.width(), (float) PlayPauseBtn.height());

        Texture MediaStepForwardBtn = greyMediaStepForwardBtn;
        Raylib.Rectangle MediaStepForwardBtnRec = new Jaylib.Rectangle(0, 0, (float) MediaStepForwardBtn.width(), (float) MediaStepForwardBtn.height());
        Jaylib.Rectangle MediaStepForwardBtnBounds = new Jaylib.Rectangle(0.75f*(musicSliderRec.width() + musicSliderRec.x()) - 1.5f*MediaStepForwardBtn.width(), screenHeight - 100 - MediaStepForwardBtn.height() / NUM_FRAMES / 2.0f, (float) MediaStepForwardBtn.width(), (float) MediaStepForwardBtn.height());

        Texture MediaStepBackwardBtn = greyMediaStepBackwardBtn;
        Jaylib.Rectangle MediaStepBackwardBtnBounds = new Jaylib.Rectangle(0.25f*(musicSliderRec.width() + musicSliderRec.x()) + (MediaStepBackwardBtn.width() / 2.0f), screenHeight - 100 - MediaStepBackwardBtn.height() / NUM_FRAMES / 2.0f, (float) MediaStepBackwardBtn.width(), (float) MediaStepBackwardBtn.height());
        Raylib.Rectangle MediaStepBackwardBtnRec = new Jaylib.Rectangle(0, 0, (float) MediaStepBackwardBtn.width(), (float) MediaStepBackwardBtn.height());

        Sound songSelectSound = LoadSound("assets/sounds/song-select.mp3");

        String musicTotalTime = ":";
        String musicCurrentTime = ":";
        String seekTime;

        float timePlayed = 0.0f;
        boolean pause = true;
        boolean pausePressed = false;
        boolean mediaStepPressed = false;
        boolean select = false;
        boolean SongQueueHover = false;
        int selectIndex = 0;
        int currentSelectSong = currentSong;
        Raylib.Vector2 mousePoint = new Jaylib.Vector2(0.0f, 0.0f);

        float scrollingOffset = -currentSong*AfacadMediumFont.baseSize()*1.5f;
        PlayMusicStream(music);
        PauseMusicStream(music);

        SetTargetFPS(30);
        while (!WindowShouldClose()) {
            sliderColor = BLACK;
            seekTime = "";
            Raylib.Rectangle SongQueueBounds = new Jaylib.Rectangle(2.0f * screenWidth / 3, screenHeight / 2.0f - AfacadMediumFont.baseSize() + scrollingOffset, (float) screenWidth /3, songNames.length*1.4f*AfacadMediumFont.baseSize());
            currentSelectSong = (int) ((int) -scrollingOffset/(AfacadMediumFont.baseSize()*1.5f));

            musicTotalTime = String.format("%d:%02d", (int) GetMusicTimeLength(music) / 60, (int) GetMusicTimeLength(music) % 60);
            musicCurrentTime = String.format("%d:%02d", (int) GetMusicTimePlayed(music) / 60, (int) GetMusicTimePlayed(music) % 60);
            UpdateMusicStream(music);
            mousePoint = GetMousePosition();

            // Select song to play using keyboard
            if (IsKeyPressed(KEY_SPACE) || IsKeyPressed(KEY_ENTER)) {
                StopMusicStream(music);
                currentSong = (int) ((int) -scrollingOffset/(AfacadMediumFont.baseSize()*1.5f));
                musicName = songNames[currentSong];
                music = LoadMusicStream(songsFolderPath + "/" + musicName);
                PlayMusicStream(music);
                pause = false;
            }

            // Pause/Resume music playing
            if (IsKeyPressed(KEY_P)) {
                pause = !pause;

                if (pause) {
                    PauseMusicStream(music);
                    PlayPauseBtn = playBtn;
                } else {
                    ResumeMusicStream(music);
                    PlayPauseBtn = pauseBtn;
                }
            }

            // Slider Hover Effect and Music Seeking
            if (CheckCollisionPointRec(mousePoint, musicSliderBounds)) {
                float seekTimeInSecs = GetMusicTimeLength(music) * (mousePoint.x() - musicSliderBounds.x()) / musicSliderBounds.width();
                seekTime = String.format("%d:%02d", (int) seekTimeInSecs / 60, (int) seekTimeInSecs % 60);
                HideCursor();
                sliderColor = MAROON;
                if (IsMouseButtonDown(MOUSE_BUTTON_LEFT)) {
                    PauseMusicStream(music);
                    SeekMusicStream(music, seekTimeInSecs);
                    if(pause){
                        PauseMusicStream(music);
                    }
                } else {
                    if(!pause) {
                        ResumeMusicStream(music);
                    }
                }
            } else {
                ShowCursor();
            }

            // Pause and Play using buttons
            if (CheckCollisionPointRec(mousePoint, btnBounds)) {
                if (IsMouseButtonDown(MOUSE_BUTTON_LEFT) && !pausePressed) {
                    pause = !pause;
                    pausePressed = true;
                    if (pause) {
                        PauseMusicStream(music);
                        PlayPauseBtn = playBtn;
                    } else {
                        ResumeMusicStream(music);
                        PlayPauseBtn = pauseBtn;
                    }
                } else if (IsMouseButtonReleased(MOUSE_BUTTON_LEFT)) {
                    pausePressed = false;
                } else {

                    if (pause) {
                        PlayPauseBtn = hoverPlayBtn;
                    } else {
                        PlayPauseBtn = hoverPauseBtn;
                    }
                }
            } else {
                if (pause) {
                    PlayPauseBtn = playBtn;
                } else {
                    PlayPauseBtn = pauseBtn;
                }
            }

            // Media Step Forward
            if(CheckCollisionPointRec(mousePoint, MediaStepForwardBtnBounds)){
                if(IsMouseButtonDown(MOUSE_BUTTON_LEFT) && !mediaStepPressed){
                    StopMusicStream(music);
                    currentSong = (currentSong + 1)%(songNames.length);
                    musicName = songNames[currentSong];
                    scrollingOffset = -currentSong*AfacadMediumFont.baseSize()*1.5f;
                    music = LoadMusicStream(songsFolderPath + "/" + musicName);
                    PlayMusicStream(music);
                    pause = false;
                    mediaStepPressed = true;
                } else if (IsMouseButtonReleased(MOUSE_BUTTON_LEFT)) {
                    mediaStepPressed = false;
                }
                MediaStepForwardBtn = hoverMediaStepForwardBtn;

            } else {
                MediaStepForwardBtn = greyMediaStepForwardBtn;
            }

            // Media Step Backward
            if(CheckCollisionPointRec(mousePoint, MediaStepBackwardBtnBounds)){
                if(IsMouseButtonDown(MOUSE_BUTTON_LEFT) && !mediaStepPressed){
                    StopMusicStream(music);
                    currentSong = (currentSong - 1)%(songNames.length);
                    currentSong = currentSong < 0 ? songNames.length+currentSong : currentSong;
                    musicName = songNames[currentSong];
                    scrollingOffset = -currentSong*AfacadMediumFont.baseSize()*1.5f;
                    music = LoadMusicStream(songsFolderPath + "/" + musicName);
                    PlayMusicStream(music);
                    pause = false;
                    mediaStepPressed = true;
                } else if (IsMouseButtonReleased(MOUSE_BUTTON_LEFT)) {
                    mediaStepPressed = false;
                }
                MediaStepBackwardBtn = hoverMediaStepBackwardBtn;

            } else {
                MediaStepBackwardBtn = greyMediaStepBackwardBtn;
            }

            // Skip Playing before or after
            if(IsKeyPressedRepeat(KEY_RIGHT) || IsKeyPressedRepeat(KEY_L) || IsKeyPressed(KEY_RIGHT) || IsKeyPressed(KEY_L)){
                float seekMusicTime = GetMusicTimePlayed(music) + 5;
                if (seekMusicTime > GetMusicTimeLength(music)) {
                    StopMusicStream(music);
                    currentSong = (currentSong + 1)%songNames.length;
                    musicName = songNames[currentSong];
                    music = LoadMusicStream(songsFolderPath + "/" + musicName);
                    PlayMusicStream(music);
                    scrollingOffset -= AfacadMediumFont.baseSize() * 1.5f;
                    pause = false;
                } else {
                    SeekMusicStream(music, seekMusicTime);
                }
            } else if(IsKeyPressedRepeat(KEY_LEFT) || IsKeyPressedRepeat(KEY_H) || IsKeyPressed(KEY_LEFT) || IsKeyPressed(KEY_H)){
                float seekMusicTime = GetMusicTimePlayed(music) - 5;
                if(seekMusicTime < 0){
                    StopMusicStream(music);
                    currentSong = (currentSong - 1)%songNames.length;
                    currentSong = currentSong < 0 ? songNames.length+currentSong : currentSong;
                    musicName = songNames[currentSong];
                    music = LoadMusicStream(songsFolderPath + "/" + musicName);
                    PlayMusicStream(music);
                    scrollingOffset += AfacadMediumFont.baseSize() * 1.5f;
                    pause = false;
                } else {
                    SeekMusicStream(music, seekMusicTime);
                }
            }

            // Scrolling State
            float oldScrollOffset = scrollingOffset;
            scrollingOffset += GetMouseWheelMove() * AfacadMediumFont.baseSize() * 1.5f;

            // Scroll using Up and Down keys
            if (IsKeyPressed(KEY_DOWN) || IsKeyPressed(KEY_J)) {
                scrollingOffset -= AfacadMediumFont.baseSize() * 1.5f;
            } else if (IsKeyPressed(KEY_UP) || IsKeyPressed(KEY_K)) {
                scrollingOffset += AfacadMediumFont.baseSize() * 1.5f;
            }

            // Song selection using click
            if(CheckCollisionPointRec(mousePoint, SongQueueBounds)){
                SongQueueHover = true;
                SetMouseCursor(MOUSE_CURSOR_POINTING_HAND);
                selectIndex = (int)Math.ceil((screenHeight / 2.0f - AfacadMediumFont.baseSize() - mousePoint.y())/(1.5f*AfacadMediumFont.baseSize()));
                if (!IsMouseButtonDown(MOUSE_BUTTON_LEFT) || select) {
                    if(IsGestureDetected(GESTURE_TAP)) select = false;
                } else {
                    scrollingOffset += selectIndex*1.5f*AfacadMediumFont.baseSize();
                    select = true;
                }
            } else {
                SongQueueHover = false;
                currentSelectSong = 0;
                SetMouseCursor(MOUSE_CURSOR_ARROW);
            }
            // Check for Scroll Bounds
            float maxScroll = (songNames.length - 1) * 1.5f * AfacadMediumFont.baseSize();
            if (scrollingOffset > 0) {
                scrollingOffset = -maxScroll;
            }
            if (maxScroll < -scrollingOffset) {
                scrollingOffset = 0;
            }

            // Play scroll sound if no song is playing
            if (pause) {
                if (oldScrollOffset != scrollingOffset) {
                    PlaySound(songSelectSound);
                }
            }

            // Next song when current song Ends
            if (musicCurrentTime.equals(musicTotalTime)){
                StopMusicStream(music);
                currentSong = (currentSong + 1)%songNames.length;
                musicName = songNames[currentSong];
                music = LoadMusicStream(songsFolderPath + "/" + musicName);
                PlayMusicStream(music);
                scrollingOffset -= AfacadMediumFont.baseSize() * 1.5f;
                pause = false;
            }

            timePlayed = GetMusicTimePlayed(music) / GetMusicTimeLength(music);

            if(timePlayed > 1.0f) timePlayed = 1.0f;

            BeginDrawing();
            ClearBackground(RAYWHITE);
            DrawText(musicName.substring(0, musicName.indexOf(".")), 30, 30 , 30, MAROON);
            DrawRectangleRec(musicSliderBounds, LIGHTGRAY);
            DrawRectangle((int) musicSliderBounds.x(), (int) musicSliderBounds.y(), (int) (timePlayed * musicSliderBounds.width()), (int) musicSliderBounds.height(), sliderColor);
            DrawRectangleLinesEx(musicSliderRec, 0, GRAY);
            Jaylib.Vector2 CurrentTimePos = new Jaylib.Vector2((int) musicSliderRec.x() - 40, (int) (musicSliderRec.height() - musicSliderBounds.height() - 5));
            Jaylib.Vector2 TotalTimePos = new Jaylib.Vector2((int) musicSliderRec.width() + 10, (int) (musicSliderRec.height() - musicSliderBounds.height() - 5));
            if (!seekTime.isEmpty()) {
                mousePoint.y((int) musicSliderRec.height() - 36);
                DrawTextEx(AfacadMediumFont, seekTime, mousePoint, 20, 1, MAROON);
                if(seekTime.compareTo(musicCurrentTime) > 0) {
                    DrawLine((int) mousePoint.x(), (int) musicSliderRec.y(), (int) mousePoint.x(), (int) musicSliderRec.height(), MAROON);
                } else {
                    DrawLine((int) mousePoint.x(), (int) musicSliderRec.y(), (int) mousePoint.x(), (int) musicSliderRec.height(), LIGHTGRAY );
                }
            }
            DrawTextEx(AfacadMediumFont, musicCurrentTime, CurrentTimePos, 20, 1, BLACK);
            DrawTextEx(AfacadMediumFont, musicTotalTime, TotalTimePos, 20, 1, BLACK);

            for (int i = 0; i < songNames.length; i++) {
                float width = 2.0f * screenWidth / 3;
                float height = screenHeight / 2.0f - AfacadMediumFont.baseSize();
                Jaylib.Vector2 pos = new Jaylib.Vector2(width, height + i * 1.5f * AfacadMediumFont.baseSize());
                pos.y(pos.y() + scrollingOffset); // Apply scrolling offset

                if (pos.y() == screenHeight / 2 - AfacadMediumFont.baseSize()) {
                    DrawText(songNames[i].substring(0, songNames[i].indexOf(".")), (int) pos.x() - 20, (int) pos.y(), 30, BLACK);
                } else {
                    if(i == (currentSelectSong - selectIndex) && SongQueueHover) {
                        DrawText(songNames[i].substring(0, songNames[i].indexOf(".")), (int) pos.x(), (int) pos.y(), 30, GRAY);
                    } else {
                        DrawText(songNames[i].substring(0, songNames[i].indexOf(".")), (int) pos.x(), (int) pos.y(), 30, LIGHTGRAY);
                    }
                }
            }

            DrawTexture(musicCover, (int) (musicSliderBounds.x() + musicCover.width() / 18), 130, LIGHTGRAY);
            Jaylib.Vector2 PlayPauseBtnBounds = new Jaylib.Vector2(btnBounds.x(), btnBounds.y());
            DrawTextureRec(PlayPauseBtn, PlayPauseRec, PlayPauseBtnBounds, WHITE);

            Jaylib.Vector2 MediaStepForwardBounds = new Jaylib.Vector2(MediaStepForwardBtnBounds.x(), MediaStepForwardBtnBounds.y());
            DrawTextureRec(MediaStepForwardBtn, MediaStepForwardBtnRec, MediaStepForwardBounds, WHITE);
            Jaylib.Vector2 MediaStepBackwardBounds = new Jaylib.Vector2(MediaStepBackwardBtnBounds.x(), MediaStepBackwardBtnBounds.y());
            DrawTextureRec(MediaStepBackwardBtn, MediaStepBackwardBtnRec, MediaStepBackwardBounds, WHITE);
            EndDrawing();
        }

            UnloadFont(AfacadMediumFont);
            UnloadSound(songSelectSound);
            UnloadMusicStream(music);
            CloseAudioDevice();
            CloseWindow();
    }

}
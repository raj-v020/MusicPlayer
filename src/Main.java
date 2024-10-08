import com.raylib.Jaylib;
import com.raylib.Raylib;

import java.io.File;
import java.io.IOException;

import static com.raylib.Jaylib.*;

class Main{

    public static void main(String[] args) throws IOException {
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

        String songsFolderPath = "music";
        File musicFolder = new File(songsFolderPath);
        File[] songs = musicFolder.listFiles();
        assert songs != null;
        String[] songNames = new String[songs.length];
        for (int i = 0; i < songs.length; i++) {
            songNames[i] = songs[i].getName();

        }

        String musicName = songNames[0];
        Music music = LoadMusicStream(songsFolderPath + "/" + musicName);
        Jaylib.Rectangle musicSliderRec = new Jaylib.Rectangle(100, 450, 400, 462);
        Jaylib.Rectangle musicSliderBounds = new Jaylib.Rectangle(musicSliderRec);
        musicSliderBounds.width(musicSliderBounds.width() - musicSliderBounds.x());
        musicSliderBounds.height(musicSliderBounds.height() - musicSliderBounds.y());

        Texture PlayPauseBtn = pauseBtn;
        float frameHeight = (float) PlayPauseBtn.height();
        Jaylib.Rectangle PlayPauseRec = new Jaylib.Rectangle(0, 0, (float) PlayPauseBtn.width(), frameHeight);
        Jaylib.Rectangle btnBounds = new Jaylib.Rectangle((musicSliderRec.width() + musicSliderRec.x()) / 2 - PlayPauseBtn.width() / 2.0f, screenHeight - 100 - PlayPauseBtn.height() / NUM_FRAMES / 2.0f, (float) PlayPauseBtn.width(), frameHeight);

//        Raylib.Rectangle songSelectBounds= new Jaylib.Rectangle(2*screenWidth/3 - 30, screenHeight/2 - AfacadMediumFont.baseSize(), 250, 30);
//        Raylib.Rectangle songSelectRec = new Jaylib.Rectangle(songSelectBounds);
//        songSelectBounds.width(songSelectBounds.x() + songSelectBounds.width());
//        songSelectBounds.height(songSelectBounds.y() + songSelectBounds.height());

        Sound songSelectSound = LoadSound("assets/sounds/song-select.mp3");

        String musicTotalTime = ":";
        String musicCurrentTime = ":";
        String seekTime;

        float timePlayed = 0.0f;
        boolean pause = true;
        boolean playing = false;
        boolean pausePressed = false;
        Raylib.Vector2 mousePoint = new Jaylib.Vector2(0.0f, 0.0f);

        float scrollingOffset = 0.0f;

        PlayMusicStream(music);
        PauseMusicStream(music);

        SetTargetFPS(30);
        while (!WindowShouldClose()) {
            sliderColor = BLACK;
            seekTime = "";

                musicTotalTime = String.format("%d:%02d", (int) GetMusicTimeLength(music) / 60, (int) GetMusicTimeLength(music) % 60);
                musicCurrentTime = String.format("%d:%02d", (int) GetMusicTimePlayed(music) / 60, (int) GetMusicTimePlayed(music) % 60);
                UpdateMusicStream(music);
                mousePoint = GetMousePosition();

                // Restart music playing (stop and play)
                if (IsKeyPressed(KEY_SPACE) || IsKeyPressed(KEY_ENTER)) {
                    StopMusicStream(music);
                    musicName = songNames[(int) ((int) -scrollingOffset/(AfacadMediumFont.baseSize()*1.5f))];
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

                // Skip Playing before or after
                if(IsKeyPressed(KEY_RIGHT)){
                    float seekMusicTime = GetMusicTimePlayed(music) + 5;
                    if(seekMusicTime < 0){
                        SeekMusicStream(music, 0.1f);
                    } else if (seekMusicTime > GetMusicTimeLength(music)) {
                        SeekMusicStream(music, GetMusicTimeLength(music));
                    } else {
                        SeekMusicStream(music, seekMusicTime);
                    }
                } else if(IsKeyPressed(KEY_LEFT)){
                    float seekMusicTime = GetMusicTimePlayed(music) - 5;
                    if(seekMusicTime < 0){
                        SeekMusicStream(music, 0);
                    } else if (seekMusicTime > GetMusicTimeLength(music)) {
                        SeekMusicStream(music, GetMusicTimeLength(music));
                    } else {
                        SeekMusicStream(music, seekMusicTime);
                    }
                }
                timePlayed = GetMusicTimePlayed(music) / GetMusicTimeLength(music);

                if (timePlayed > 1.0f) timePlayed = 1.0f;

            float oldScrollOffset = scrollingOffset;
            scrollingOffset += GetMouseWheelMove() * AfacadMediumFont.baseSize() * 1.5f;

            if (IsKeyPressed(KEY_DOWN)) {
                scrollingOffset -= AfacadMediumFont.baseSize() * 1.5f;
            } else if (IsKeyPressed(KEY_UP)) {
                scrollingOffset += AfacadMediumFont.baseSize() * 1.5f;
            }

            if (scrollingOffset > 0) {
                scrollingOffset = 0;
            }
            float maxScroll = (songNames.length - 1) * 1.5f * AfacadMediumFont.baseSize();
            if (maxScroll < -scrollingOffset) {
                scrollingOffset = -maxScroll;
            }

            if (!playing) {
                if (oldScrollOffset != scrollingOffset) {
                    PlaySound(songSelectSound);
                }
            }

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
            }
            DrawTextEx(AfacadMediumFont, musicCurrentTime, CurrentTimePos, 20, 1, BLACK);
            DrawTextEx(AfacadMediumFont, musicTotalTime, TotalTimePos, 20, 1, BLACK);

            for (int i = 0; i < songNames.length; i++) {
                float height = 2.0f * screenWidth / 3;
                float width = screenHeight / 2.0f + -AfacadMediumFont.baseSize();
                Jaylib.Vector2 pos = new Jaylib.Vector2(height, width + i * 1.5f * AfacadMediumFont.baseSize()); // Calculate drawing position
                pos.y(pos.y() + scrollingOffset); // Apply scrolling offset

                if (pos.y() == screenHeight / 2 - AfacadMediumFont.baseSize()) {
                    DrawText(songNames[i].substring(0, songNames[i].indexOf(".")), (int) pos.x(), (int) pos.y(), 30, BLACK);
                } else {
                    DrawText(songNames[i].substring(0, songNames[i].indexOf(".")), (int) pos.x(), (int) pos.y(), 30, LIGHTGRAY);
                }
            }

            DrawTexture(musicCover, (int) (musicSliderBounds.x() + musicCover.width() / 18), 130, LIGHTGRAY);
            Jaylib.Vector2 PlayPauseBtnBounds = new Jaylib.Vector2(btnBounds.x(), btnBounds.y());
            DrawTextureRec(PlayPauseBtn, PlayPauseRec, PlayPauseBtnBounds, WHITE);
            EndDrawing();
        }

            UnloadFont(AfacadMediumFont);
            UnloadSound(songSelectSound);
            UnloadMusicStream(music);
            CloseAudioDevice();
            CloseWindow();
    }

}
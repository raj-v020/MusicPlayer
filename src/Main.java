import com.raylib.Jaylib;
import com.raylib.Raylib;

import static com.raylib.Jaylib.*;

class Main{

    public static void main(String[] args)
    {
        final int NUM_FRAMES = 3;
        int screenWidth = 800;
        int screenHeight = 600;
        Raylib.Color sliderColor;
        InitWindow(screenWidth, screenHeight, "raylib [audio] example - music playing (streaming)");

        InitAudioDevice();              // Initialize audio device

        Texture hoverPlayBtn = LoadTexture("assets/play.png");
        Texture hoverPauseBtn = LoadTexture("assets/pause.png");
        Texture playBtn = LoadTexture("assets/play-grey.png");
        Texture pauseBtn = LoadTexture("assets/pause-grey.png");

        Texture PlayPauseBtn = pauseBtn;
        float frameHeight = (float)PlayPauseBtn.height();
        Jaylib.Rectangle PlayPauseRec = new Jaylib.Rectangle(0, 0, (float)PlayPauseBtn.width(), frameHeight);
        Jaylib.Rectangle btnBounds = new Jaylib.Rectangle(screenWidth/2.0f - PlayPauseBtn.width()/2.0f, screenHeight - 100 - PlayPauseBtn.height()/NUM_FRAMES/2.0f, (float)PlayPauseBtn.width(), frameHeight);

        int PlayPauseBtnState = 0;

        Music music = LoadMusicStream("music/Jingle-Punks.mp3");
        Jaylib.Rectangle musicSliderRec = new Jaylib.Rectangle(200, 450, 400, 12);
        Jaylib.Rectangle musicSliderBounds = new Jaylib.Rectangle(musicSliderRec);
        musicSliderBounds.width(musicSliderBounds.x() + musicSliderBounds.width());
        musicSliderBounds.width(musicSliderBounds.y() + musicSliderBounds.height());

        PlayMusicStream(music);
        int musicTotalTimeinSecs = (music.frameCount() / music.stream().sampleRate());
        String musicTotalTime = String.format("%d:%02d", musicTotalTimeinSecs/60, musicTotalTimeinSecs%60);
        System.out.println(musicTotalTime);

        float timePlayed = 0.0f;        // Time played normalized [0.0f..1.0f]
        boolean pause = false;             // Music playing paused
        boolean pausePressed = false;
        Raylib.Vector2 mousePoint = new Jaylib.Vector2(0.0f, 0.0f);

        SetTargetFPS(30);               // Set our game to run at 30 frames-per-second
        //--------------------------------------------------------------------------------------
        // Main game loop
        while (!WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            UpdateMusicStream(music);  // Update music buffer with new stream data
            mousePoint = GetMousePosition();
            sliderColor = BLACK;

            // Restart music playing (stop and play)
            if (IsKeyPressed(KEY_SPACE))
            {
                StopMusicStream(music);
                PlayMusicStream(music);
            }

            // Pause/Resume music playing
            if (IsKeyPressed(KEY_P))
            {

                pause = !pause;

                if (pause) {
                    PauseMusicStream(music);
                    PlayPauseBtn = playBtn;
                }
                else {
                    ResumeMusicStream(music);
                    PlayPauseBtn = pauseBtn;
                }
            }

            if (CheckCollisionPointRec(mousePoint, musicSliderBounds)){
                sliderColor = MAROON;
            }
            if (CheckCollisionPointRec(mousePoint, btnBounds)){
                if (IsMouseButtonDown(MOUSE_BUTTON_LEFT) && !pausePressed){
                    pause = !pause;
                    pausePressed = true;
                    if (pause) {
                        PauseMusicStream(music);
                        PlayPauseBtn = playBtn;
                    }
                    else {
                        ResumeMusicStream(music);
                        PlayPauseBtn = pauseBtn;
                    }
                }else if(IsMouseButtonReleased(MOUSE_BUTTON_LEFT)){
                    pausePressed = false;
                } else{

                    if(pause){
                        PlayPauseBtn = hoverPlayBtn;
                    } else{
                        PlayPauseBtn = hoverPauseBtn;
                    }
                }

            } else{
                if(pause){
                    PlayPauseBtn = playBtn;
                } else{
                    PlayPauseBtn = pauseBtn;
                }
            }

            // Get normalized time played for current music stream
            timePlayed = GetMusicTimePlayed(music)/GetMusicTimeLength(music);

            if (timePlayed > 1.0f) timePlayed = 1.0f;   // Make sure time played is no longer than music
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            BeginDrawing();

            ClearBackground(RAYWHITE);

            DrawText("MUSIC SHOULD BE PLAYING!", 255, 150, 20, LIGHTGRAY);

            DrawRectangleRec(musicSliderRec, LIGHTGRAY);
            DrawRectangle((int)musicSliderRec.x(), (int)musicSliderRec.y(), (int)(timePlayed*400.0f), (int)musicSliderRec.height(), sliderColor);
            DrawRectangleLinesEx(musicSliderRec, 0, GRAY);

            DrawText("PRESS SPACE TO RESTART MUSIC", 215, 250, 20, LIGHTGRAY);
            DrawText("PRESS P TO PAUSE/RESUME MUSIC", 208, 280, 20, LIGHTGRAY);
            Jaylib.Vector2 PlayPauseBtnBounds = new Jaylib.Vector2(btnBounds.x(), btnBounds.y());
            DrawTextureRec(PlayPauseBtn, PlayPauseRec, PlayPauseBtnBounds, WHITE);
            EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        UnloadMusicStream(music);   // Unload music stream buffers from RAM

        CloseAudioDevice();         // Close audio device (music streaming is automatically stopped)

        CloseWindow();              // Close window and OpenGL context
        //--------------------------------------------------------------------------------------
    }


}
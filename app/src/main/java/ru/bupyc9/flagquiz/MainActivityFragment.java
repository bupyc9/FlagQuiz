package ru.bupyc9.flagquiz;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.security.SecureRandom;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    // Строка, используемая при регистрации сообщений об ошибках
    private static final String TAG = "FlagQuiz Activity";
    private static final int FLAGS_IN_QUIZ = 10;
    private List<String> fileNameList; // Имена файлов с флагами
    private List<String> quizCountriesList; // Страны текущей викторины
    private Set<String> regionsSet; // Регионы текущей викторины
    private String correctAnswer; // Правильная страна для текущего флага
    private int totalGuesses; // Количество попыток
    private int correctAnswers; // Количество правильных ответов
    private int guessRows; // Количество строк с кнопками вариантов
    private SecureRandom random; // Генератор случайных чисел
    private Handler handler; // Для задержки загрузки следующего флага
    private Animation shakeAnimation; // Анимация неправильного ответа
    private LinearLayout quizLinearLayout; // Макет с викториной
    private TextView questionNumberTextView; // Номер текущего вопроса
    private ImageView flagImageView; // Для вывода флага
    private LinearLayout[] guessLinearLayouts; // Строки с кнопками
    private TextView answerTextView; // Для правильного ответа

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }
}

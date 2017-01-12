package ru.bupyc9.flagquiz;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

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
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        fileNameList = new ArrayList<>();
        quizCountriesList = new ArrayList<>();
        random = new SecureRandom();
        handler = new Handler() {
            @Override
            public void close() {

            }

            @Override
            public void flush() {

            }

            @Override
            public void publish(LogRecord record) {

            }
        };

        // Загрузка анимации для неправильных ответов
        shakeAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.incorrect_shake);
        shakeAnimation.setRepeatCount(3); // Анимация повторяется 3 раза

        // Получение ссылок на компоненты графического интерфейса
        quizLinearLayout = (LinearLayout) view.findViewById(R.id.quizLinearLayout);
        questionNumberTextView = (TextView) view.findViewById(R.id.questionNumberTextView);
        flagImageView = (ImageView) view.findViewById(R.id.flagImageView);
        guessLinearLayouts = new LinearLayout[4];
        guessLinearLayouts[0] = (LinearLayout) view.findViewById(R.id.row2LinearLayout);
        guessLinearLayouts[2] = (LinearLayout) view.findViewById(R.id.row3LinearLayout);
        guessLinearLayouts[3] = (LinearLayout) view.findViewById(R.id.row4LinearLayout);
        answerTextView = (TextView) view.findViewById(R.id.answerTextView);

        // Настройка слушателей для кнопок ответов
        for (LinearLayout row : guessLinearLayouts) {
            for (int column = 0; column < row.getChildCount(); column++) {
                Button button = (Button) row.getChildAt(column);
                button.setOnClickListener(guessButtonListener);
            }
        }

        questionNumberTextView.setText(getString(R.string.question, 1, FLAGS_IN_QUIZ));

        return view;
    }

    // Обновление guessRows на основании значения SharedPreferences
    public void updateGuessRows(SharedPreferences sharedPreferences) {
        // Получение количества вариантов ответов
        String choices = sharedPreferences.getString(MainActivity.CHOICES, null);
        guessRows = Integer.parseInt(choices) / 2;
        // Все компоненты LinearLayout скрываются
        for (LinearLayout layout : guessLinearLayouts) {
            layout.setVisibility(View.GONE);
        }

        // Отображение нужных компонентов LinearLayout
        for (int row = 0; row < guessRows; row++) {
            guessLinearLayouts[row].setVisibility(View.VISIBLE);
        }
    }

    // Обновление выбранных регионов по данным из SharedPreferences
    public void updateRegions(SharedPreferences sharedPreferences) {
        regionsSet = sharedPreferences.getStringSet(MainActivity.REGIONS, null);
    }

    // Настройка и запуск следующей серии вопросов
    public void resetQuiz() {
        // Использование AssetManager для получения имен файлов изображений
        AssetManager assets = getActivity().getAssets();
        fileNameList.clear(); // Пустой список имен файлов
        try {
            // Перебрать все регионы
            for (String region : regionsSet) {
                String[] paths = assets.list(region);
                for (String path : paths) {
                    fileNameList.add(path.replace(".png", ""));
                }
            }
        } catch (IOException exception) {
            Log.e(TAG, "Error loading image file names", exception);
        }

        correctAnswers = 0; // Сброс количества правильных ответов
        totalGuesses = 0; // Сброс общего количеста попыток
        quizCountriesList.clear();
        int flagCounter = 1;
        int numberOfFlags = fileNameList.size();
        // Добавление FLAG_IN_QUIZ случайных файлов в quizCountriesList
        while (flagCounter <= FLAGS_IN_QUIZ) {
            int randomIndex = random.nextInt(numberOfFlags);
            // Получение случайного имени файла
            String fileName = fileNameList.get(randomIndex);
            // Если регион включен, но еще не был выбран
            if (!quizCountriesList.contains(fileName)) {
                quizCountriesList.add(fileName);
                ++flagCounter;
            }
        }

        loadNextFlag(); // Запустить викторину загрузкой первого флага
    }

    // Загрузка следующего флага после правильного ответа
    private void loadNextFlag() {
        // Получение имени файла следующего флага и удаление его из списка
        String nextImage = quizCountriesList.remove(0);
        correctAnswer = nextImage; // Обновление правильного ответа
        answerTextView.setText("");
        // Отображение номера текущего вопроса
        questionNumberTextView.setText(getString(R.string.question, (correctAnswers + 1), FLAGS_IN_QUIZ));
        // Извлечение региона из имени следующего изображения
        String region = nextImage.substring(0, nextImage.indexOf('-'));
        // Использование AssetManager для загрузки следующего изображения
        AssetManager assetManager = getActivity().getAssets();
        // Получение объекта InputStream для ресурса следующего флага и попытка использования InputStream
        try (InputStream stream = assetManager.open(region + "/" + nextImage + ".png")) {
            // Загрзука графики в виде Drawable и вывод на flagImageView
            Drawable flag = Drawable.createFromStream(stream, nextImage);
            flagImageView.setImageDrawable(flag);
            animate(false); // Анимация появления флага на экране
        } catch (IOException exception) {
            Log.e(TAG, "Error loading" + nextImage, exception);
        }

        Collections.shuffle(fileNameList);
        // Помещение правильного ответа в конец fileNameList
        int correct = fileNameList.indexOf(correctAnswer);
        fileNameList.add(fileNameList.remove(correct));

        // Добавление 2, 4, 6, 8 кнопок в зависимости от значения guessRows
        for (int row = 0; row < guessRows; row++) {
            // Размещение кнопок в currentTableRow
            for (int column = 0; column < guessLinearLayouts[row].getChildCount(); column++) {
                // Получение ссылки на Button
                Button newGuessButton = (Button) guessLinearLayouts[row].getChildAt(column);
                newGuessButton.setEnabled(true);
                String filename = fileNameList.get((row * 2) + column);
                newGuessButton.setText(getCountryName(filename));
            }
        }

        // Случаная замена одной кнопки правильным ответом
        int row = random.nextInt(guessRows); // Выбор случайной строки
        int column = random.nextInt(2); // Выбор случаного столбца

        LinearLayout randomRow = guessLinearLayouts[row];
        String countryName = getCountryName(correctAnswer);
        ((Button) randomRow.getChildAt(column)).setText(countryName);
    }

    // Метод разбирает имя файла с флагом и возвращает название страны
    private String getCountryName(String name) {
        return name.substring(name.indexOf('-') + 1).replace('_', ' ');
    }

    // Весь макет quizLinearLayout появляется или исчезает с экрана
    private void animate(boolean animateOut) {
        // Предотвращение анимации интерфейса для первого флага
        if (correctAnswers == 0) {
            return;
        }

        // Вычисление координат центра
        int centerX = (quizLinearLayout.getLeft() + quizLinearLayout.getRight()) / 2;
        int centerY = (quizLinearLayout.getTop() + quizLinearLayout.getBottom()) / 2;

        // Вычисление радиуса анимации
        int radius = Math.max(quizLinearLayout.getWidth(), quizLinearLayout.getHeight());
        Animator animator;

        // Если изображение должно исчезать с экрана
        if (animateOut) {
            // Создание круговой анимации
            animator = ViewAnimationUtils.createCircularReveal(quizLinearLayout, centerX, centerY, radius, 0);
            animator.addListener(new AnimatorListenerAdapter() {
                // Вызывается при завершение анимации
                @Override
                public void onAnimationEnd(Animator animation) {
                    loadNextFlag();
                }
            });
        } else {
            animator = ViewAnimationUtils.createCircularReveal(quizLinearLayout, centerX, centerY, 0, radius);
        }

        animator.setDuration(500);
        animator.start();
    }

    // Вызывается при нажатии кнопки ответа
    private View.OnClickListener guessButtonListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Button guessButton = ((Button) v);
            String guess = guessButton.getText().toString();
            String answer = getCountryName(correctAnswer);
            ++totalGuesses;

            if (guess.equals(answer)) { // Если ответ правильный
                ++correctAnswers;
                answerTextView.setText(answer + "!");
                answerTextView.setTextColor(getResources().getColor(
                        R.color.correct_answer,
                        getContext().getTheme()
                ));

                disableButtons();

                if (correctAnswers == FLAGS_IN_QUIZ) {
                    // DialogFragment для вывода статистики и перезапуска
                    DialogFragment quizResults = new DialogFragment() {
                        @NonNull
                        @Override
                        public Dialog onCreateDialog(Bundle bundle) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage(getString(R.string.results, totalGuesses, (1000 / (double) totalGuesses)));
                            // Кнопка сброса
                            builder.setPositiveButton(R.string.reset_quiz, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    resetQuiz();
                                }
                            });

                            return builder.create();
                        }
                    };

                    quizResults.setCancelable(false);
                    quizResults.show(getFragmentManager(), "quiz results");
                } else { // Ответ правильный но викторина не закончена
                    // Загрузка следующего флага после двухсекундной задержки
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            animate(true);
                        }
                    }, 2000);
                }
            } else { // Неправльный ответ
                flagImageView.startAnimation(shakeAnimation);
                answerTextView.setText(R.string.incorrect_answer);
                answerTextView.setTextColor(getResources().getColor(R.color.incorrect_answer, getContext().getTheme()));
                guessButton.setEnabled(false);
            }
        }
    };

    private void disableButtons() {
        for (int row = 0; row < guessRows; row++) {
            LinearLayout guessRow = guessLinearLayouts[row];
            for (int i = 0; i < guessRow.getChildCount(); i++) {
                guessRow.getChildAt(i).setEnabled(false);
            }
        }
    }
}
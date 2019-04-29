package com.gianlu.commonutils.CasualViews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.R;

public class FakeLoadingWithLogoView extends FrameLayout {
    private ImageView logo;
    private ProgressBar bar;
    private int fastLoadDuration = 5000;
    private float fastLoadPercent = 70;
    private int slowLoadDuration = 10000;
    private boolean possiblyShort;

    public FakeLoadingWithLogoView(@NonNull Context context) {
        this(context, null, 0);
    }

    public FakeLoadingWithLogoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FakeLoadingWithLogoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        inflate(context, R.layout.view_fake_loading_with_logo, this);

        setBackgroundColor(CommonUtils.resolveAttrAsColor(context, android.R.attr.colorBackground));

        logo = findViewById(R.id.fakeLoadingWithLogo_logo);
        bar = findViewById(R.id.fakeLoadingWithLogo_bar);
    }

    public void setTiming(int fastLoadDuration, float fastLoadPercent, int slowLoadDuration) {
        this.fastLoadDuration = fastLoadDuration;
        this.fastLoadPercent = fastLoadPercent;
        this.slowLoadDuration = slowLoadDuration;
    }

    public void startFakeAnimation(boolean possiblyShort) {
        this.possiblyShort = possiblyShort;

        setVisibility(VISIBLE);
        bar.setPercentage(0);
        bar.exponentialTo(fastLoadDuration, fastLoadPercent, new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                bar.setPercentage(0);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bar.linearTo(slowLoadDuration, 100, null);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    private void fadeAway() {
        AlphaAnimation animation = new AlphaAnimation(1, 0);
        animation.setDuration(possiblyShort ? 500 : 1500);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setVisibility(GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        startAnimation(animation);
    }

    public void endFakeAnimation() {
        endFakeAnimation(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                fadeAway();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    public void endFakeAnimation(@NonNull Animation.AnimationListener listener) {
        bar.accelerateTo(1000, 100, listener);
    }

    public void setLogoRes(@DrawableRes int res) {
        logo.setImageResource(res);
    }

    private static class ExponentialInterpolator implements Interpolator {
        private final float a;
        private final float b;

        ExponentialInterpolator(float a, float b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public float getInterpolation(float input) {
            return (float) (-Math.pow(a, -b * input) + 1f);
        }
    }

    public static class ProgressBar extends View {
        private final Paint paint;
        private float percentage = 0;
        private WaitEnd waitEnd;

        public ProgressBar(Context context) {
            this(context, null, 0);
        }

        public ProgressBar(Context context, @Nullable AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public ProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            setWillNotDraw(false);

            paint = new Paint();
            paint.setColor(ContextCompat.getColor(context, R.color.colorAccent));
            setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, context.getResources().getDisplayMetrics()));
        }

        public void setStrokeWidth(float width) {
            paint.setStrokeWidth(width);
            requestLayout();
        }

        public void setPercentage(float percentage) {
            this.percentage = percentage;
            invalidate();
        }

        public void animateTo(int duration, float to, @NonNull Interpolator interpolator, @Nullable Animation.AnimationListener listener) {
            float startFrom = percentage;
            float toGo = to - startFrom;

            Animation anim = new Animation() {
                {
                    setInterpolator(interpolator);
                    setDuration(duration);
                }

                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    setPercentage(startFrom + interpolatedTime * toGo);
                }
            };

            anim.setAnimationListener(listener);

            if (getAnimation() != null) {
                waitEnd = () -> startAnimation(anim);

                getAnimation().setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (waitEnd != null) waitEnd.ended();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });

                clearAnimation();
            } else {
                startAnimation(anim);
            }
        }

        public void accelerateTo(int duration, float to, @Nullable Animation.AnimationListener listener) {
            animateTo(duration, to, new AccelerateInterpolator(), listener);
        }

        public void linearTo(int duration, float to, @Nullable Animation.AnimationListener listener) {
            animateTo(duration, to, new LinearInterpolator(), listener);
        }

        public void exponentialTo(int duration, float to, @Nullable Animation.AnimationListener listener) {
            animateTo(duration, to, new ExponentialInterpolator(1.5f, 11f), listener);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                    MeasureSpec.makeMeasureSpec((int) paint.getStrokeWidth(), MeasureSpec.EXACTLY));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            int width = (int) (getWidth() / 100f * percentage);
            canvas.drawLine(0, getHeight() / 2f, width, getHeight() / 2f, paint);
        }

        private interface WaitEnd {
            void ended();
        }
    }
}

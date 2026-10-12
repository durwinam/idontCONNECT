# ساخت APK در GitHub Actions (بدون نصب Android Studio)

این پروژه Workflow آماده در مسیر `.github/workflows/android-build.yml` دارد. با اجرای Workflow، GitHub روی runner ابری JDK 17، Gradle و Android SDK را آماده می‌کند، تست‌های واحد را اجرا می‌کند، سپس APK آزمایشی را می‌سازد و به‌عنوان Artifact قابل دانلود ذخیره می‌کند.

## روش راه‌اندازی

1. وارد https://github.com/new شوید و یک repository خالی بسازید؛ مثلاً `idontCONNECT`.
2. در رایانه، ZIP سورس را استخراج کنید. وارد پوشه پروژه شوید؛ باید `settings.gradle.kts`، `app/` و `.github/` را همان‌جا ببینید.
3. تمام محتویات داخل پوشه پروژه را در ریشه repository بارگذاری و commit کنید. خود ZIP را به‌تنهایی در repository قرار ندهید؛ GitHub باید فایل `.github/workflows/android-build.yml` را در مسیر دقیق خودش ببیند.
4. در repository، زبانه **Actions** را باز کنید و Workflow با نام **Android source validation** را انتخاب کنید.
5. روی **Run workflow** بزنید. منتظر بمانید تا مراحل Unit tests و Assemble debug APK کامل شوند.
6. اگر اجرا موفق بود، پایین صفحه اجرای Workflow در بخش **Artifacts**، فایل `idontCONNECT-debug-apk` ظاهر می‌شود. آن را دانلود و از حالت ZIP خارج کنید؛ APK داخل آن قرار دارد.

## اگر خطا رخ داد

- روی مرحله قرمز کلیک کنید و متن کامل خطا را بردارید.
- اگر خطا مربوط به تست یا کامپایل Kotlin است، ابتدا باید سورس اصلاح شود؛ صرفاً اجرای مجدد خطا را رفع نمی‌کند.
- اگر Artifact تولید نشد، APK ساخته نشده یا workflow به مرحله upload نرسیده است.

## محدودیت‌های مهم

- این Workflow فقط APK **debug** می‌سازد؛ APK انتشار عمومی نیست و کلید امضای release ندارد.
- سورس فعلی هنوز موتور native واقعی Xray/libXray را بسته‌بندی و یکپارچه نکرده است. حتی اگر APK با موفقیت ساخته شود، نباید آن را VPN آماده اتصال تلقی کرد.
- ساخت موفق در GitHub Actions تا زمانی که Workflow واقعاً اجرا نشده باشد تضمین‌شده نیست.

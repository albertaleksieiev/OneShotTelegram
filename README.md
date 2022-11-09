# One-shot telegram bot

TelegramBot SDK with Android-like UI view architecture

![](https://github.com/albertaleksieiev/OneShotTelegram/raw/localization/docs/img1.gif)

## Features
- 🖌 Customizable view with a bunch of UI methods, each telegram message has associated java class with own lifecycle.
- 🏴󠁧󠁢󠁥󠁮󠁧󠁿 Localization, easy just define a class!
- 💾 Persistence, when server restarts or user clicks on 1year message you will receive an action to saved view.
- 💉 Dependency injection, receive in your view all of your dependencies!
- 🔑 Thread local authorization, in any part of the app just call `getUser` to receive user. It can be usefull in spring app.
- ✨ Compatible with kotlin, create any view in 5 lines of code.

### Create a view
To create a view aka page, you just need to extends from `BotView` class

```
public class HomeView extends BotView {
    public HomeView(BotContext context) { super(context); }
    
    @Override
    public String draw(Storage data) {
        return "Welcome Home 🏠"; // Will be sent as TextMessage to user
    }

    @Override
    public String name() {
        return "Home 🌏"; // This name will appear on menu
    }
}
```
![](https://github.com/albertaleksieiev/OneShotTelegram/raw/localization/docs/img2.png)

### Start Bot
To register and start bot you need use `BotCenter`

```
private static final String BOT_TOKEN = ; // Retrieve it from here https://telegram.me/BotFather
private static final String BOT_NAME = "OneShot bot";

public static void main(String[] args) {
    BotCenter botCenter = new BotCenter.Builder(BOT_NAME, BOT_TOKEN, HomeView.class /*Initial View*/).build();
    botCenter.start();

    while (true) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

### Installation
Gradle

```
repositories { 
    jcenter()
    maven { url "https://jitpack.io" }
}
dependencies {
     compile 'com.github.albertaleksieiev:OneShotTelegram:master-SNAPSHOT'
}
```  

### Add other view, display it inside menu and navigate to other view
##### 1. Create settings view where user can configure name and save it to `SessionStorage`

```
public class SettingsView extends BotView {
    public SettingsView(BotContext context) {
        super(context);
    }

    @Override
    public String draw(Storage data) {
        return "Enter your name";
    }

    @Override
    public String name() {
        return "Configure Name ⚙️";
    }

    @Override
    public void onTextReceived(Message message) {
        super.onTextReceived(message);

        // Get entered text
        String text = message.getText();

        // Save it into session storage
        SessionStorage sessionStorage = getContext().getSessionStorage();
        sessionStorage.put(HomeView.STORAGE_KEY_USERNAME, text);

        // And navigate to home view
        navigate(HomeView.class, true /* Send it as a new message */);
    }
}
```

##### 2. Add settings into home menu links and display user name

```
public class HomeView extends BotView {
    ...
    
    public static final String STORAGE_KEY_USERNAME = "STORAGE_KEY_USERNAME";
    
    @Override
    public String draw(Storage data) {
        String userName = getContext().getSessionStorage().get(STORAGE_KEY_USERNAME, String.class);
        return "Welcome Home " +
                (userName == null ? "" : userName)
                + "🏠"; // Will be sent as TextMessage to user
    }
    
    ...
    @Override
    public <BT extends BotView> Class<BT>[] menuLinkStates() {
        return new Class[] { SettingsView.class };
    }
}
```

![](https://github.com/albertaleksieiev/OneShotTelegram/raw/localization/docs/img3.gif)

### Navigate to other view with arguments

##### 1. To navigate to view with args, use function `navigate` with `data` argument

```
public class SettingsView extends BotView {
   
    @Override
    public void onTextReceived(Message message) {
        ...
        Storage data = new Storage();
        data.put(HomeView.ARG_JUST_CONFIGURED, true);

        // And navigate to home view
        navigate(HomeView.class, data, true /* Send it as a new message*/);
    }
}
```

##### 2. To retrieve data and display it use `data` argument from `draw` function or method `getData()`

```
public class HomeView extends BotView {
    public static final String ARG_JUST_CONFIGURED = "ARG_JUST_CONFIGURED";
    ...
    @Override
    public String draw(Storage data) {
        String userName = getContext().getSessionStorage().get(STORAGE_KEY_USERNAME, String.class);
        Boolean justConfigured = data.get(ARG_JUST_CONFIGURED, Boolean.class);
        
        return "Welcome Home " +
                (userName == null ? "" : userName)
                + "🏠"
                + (justConfigured != null && justConfigured ? "\nAnd thanks for configuration!" : "");
    }
}
```

### Inject arguments into BotView constructor
`OneShotTelegramBot` take care of object initialization by self, so you need to use simple Dependency Injection by providing DependecyProvider instance. To do this create a class with `@DependencyProvider` anotation and  public method with `@InstanceProvider` which provides instance.

##### 1. Providing `DependecyProvider`

```
@DependencyProvider
public static class MyDependencyProvider {
    DBInMemory dbInMemory;

    public MyDependencyProvider(DBInMemory dbInMemory) {
        this.dbInMemory = dbInMemory;
    }

    @InstanceProvider
    public DBInMemory providesDBInMemory() {
        return dbInMemory;
    }
}

// Simple implementation of DB
public static class DBInMemory {
    private static String userToken;

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        DBInMemory.userToken = userToken;
    }
}
```

##### 2. Registration provider

```
...
private static DBInMemory db = new DBInMemory();
     
BotCenter botCenter = new BotCenter.Builder(BOT_NAME,
             BOT_TOKEN,
             new MyDependencyProvider(db),
             HomeView.class /*Initial View*/)
             	.setDependecyProvider(new MyDependencyProvider(db))
             	.build();
```

##### 3. Add argument to view constructor

```
public class ConfigureTokenView extends BotView {
    private final Main.DBInMemory db;

    public ConfigureTokenView(BotContext context, DBInMemory db /*Will be injected*/) {
        super(context);
        this.db = db;
    }

    @Override
    public String draw(Storage data) {
        String token = db.getUserToken();
        return "Your token `" + (token == null ? "?" : token) +"`\n"
                + "Enter new token: ";
    }

    @Override
    public void onTextReceived(Message message) {
        super.onTextReceived(message);
        String text = message.getText();

        db.setUserToken(text);

        navigate(HomeView.class, true);
    }

    @Override
    public String name() {
        return "Configure Token 🔑";
    }
}
```

##### 4. Display newly configured token

```
public class HomeView extends BotView {
    private final Main.DBInMemory db;
    ...
    
    public HomeView(BotContext context, DBInMemory db /* Injection */) {
        super(context);
        this.db = db;
    }
    
    @Override
    public String draw(Storage data) {
        ...
        String token = db.getUserToken();
        
        return "Welcome Home " +
                (userName == null ? "" : userName)
                + "🏠"
                + "Your token `" + (token == null ? "?" : token) +"`\n"
                + (justConfigured != null && justConfigured ? "\nAnd thanks for configuration!" : "");
    }
   
    @Override
    public <BT extends BotView> Class<BT>[] menuLinkStates() { return new Class[] { SettingsView.class, ConfigureTokenView.class }; }
}
```

![](https://github.com/albertaleksieiev/OneShotTelegram/raw/localization/docs/img4.gif)

### Persistence `BotView` instance
By default your view and data will be saved to disk in case of restore state, restoring used in case of clicking on the button on old messages. So data needs to be serializable, but you can override `onSaveInstanceState` and `onRestoreInstanceState`.

You can disable saving view to disk by `BotCenter.Builder::setInstanceSavingEnabled(false)`. 

### Localization

To use localization you have to do a few actions:
##### 1. Create resource class
```
public class R {
    public static final String LOCALE_RU = "ru";
    public static final String LOCALE_EN = "en";
    
    public enum string {
        LocalizationViewTitle("Localization Example", "Пример локализации"),
        LocalizationViewDescription("Here you see localization, press button below to change language", "Здесь вы видите локализацию, поменяйте язык ниже.");

        public final String en;
        public final String rus;

        private string(String en, String rus) {
            this.en = en;
            this.rus = rus;
        }
        public static string valueOf(int ordinal) {
            return string.values()[ordinal];
        }
    }
}
```

##### 2. Implement `BotCenter.LocalizationProvider` and pass it to `BotCenter.Builder`
```
builder.setLocalization(new BotCenter.LocalizationProvider() {
    @Override
    public String getString(String locale, Integer stringId) { // Here you return string by string ID and locale
        R.string string = R.string.valueOf(stringId);
        if (locale.equals(LOCALE_EN)) {
            return string.en;
        } else if (locale.equals(LOCALE_RU)) {
            return string.rus;
        }
        
        return null; // Catch issue here
    }

    @Override
    public String getLocale(Update update) {
        return db.getLocale(update); // get locale for selected update see `DBInMemory` for more detais
    }
})
```

##### 3. Use `getString` in your BotView
```
...
@Override
public String name() {
    return getString(R.string.LocalizationViewTitle.ordinal()); // use enum::ordinal as ID
}
...
```


See `LocalizationExampleView.java` for more details.

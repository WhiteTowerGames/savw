package com.savw.shout;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.savw.word.ShoutWord;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static com.savw.shout.Shouts.*;

/// # AbstractShout
/// Abstract class representing a shout in the game.
/// This class contains common properties and methods for all shouts.
public abstract class AbstractShout {

    private final String name;
    private final String description;
    private final ShoutWord firstWord;
    private final ShoutWord secondWord;
    private final ShoutWord thirdWord;
    private final ResourceLocation iconLocation;
    private final ShoutWord[] allWords;
    private final ResourceKey<Level> dimension;

    protected AbstractShout(String name, String description,
                            ShoutWord firstWord, ShoutWord secondWord, ShoutWord thirdWord,
                            ResourceLocation iconLocation, ResourceKey<Level> dimension) {
        this.name = name;
        this.description = description;
        this.firstWord = firstWord;
        this.secondWord = secondWord;
        this.thirdWord = thirdWord;
        this.iconLocation = iconLocation;
        this.allWords = new ShoutWord[]{firstWord, secondWord, thirdWord};
        this.dimension = dimension;
    }

    /// ### createShout
    /// Factory method to create singleton instances of AbstractShout subclasses.
    ///
    /// @param shoutClass   the class of the shout to be created
    /// @param name         the name of the shout
    /// @param description  the description of the shout
    /// @param firstWord    the first word of the shout
    /// @param secondWord   the second word of the shout
    /// @param thirdWord    the third word of the shout
    /// @param iconLocation the resource location of the shout's icon
    /// @param <S>          the type of the shout, a class that extends AbstractShout.
    /// @param dimension    the dimension in which the shout is unlocked
    /// @return an instance of the specified shout subclass
    public static <S extends AbstractShout> S createShout(Class<S> shoutClass, String name, String description,
                                                          ShoutWord firstWord, ShoutWord secondWord, ShoutWord thirdWord,
                                                          ResourceLocation iconLocation, ResourceKey<Level> dimension) {
        try {
            Constructor<S> constructor = shoutClass.getDeclaredConstructor(String.class, String.class, ShoutWord.class, ShoutWord.class,
                    ShoutWord.class, ResourceLocation.class, ResourceKey.class);
            constructor.setAccessible(true); // Allow access to private constructor
            return constructor.newInstance(name, description, firstWord, secondWord, thirdWord, iconLocation, dimension);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException("Failed to create shout instance", e);
        }
    }

    /// ### SHOUT_NAME_CODEC
    /// Codec for encoding and decoding AbstractShout instances based on their name.
    /// The codec uses a flatXmap to map between the shout name and the corresponding AbstractShout (or subclass thereof) instance.
    ///
    /// If the shout name is not found, an error is returned.
    ///
    /// @see com.savw.shout.Shouts#ALL_SHOUTS
    public static final Codec<AbstractShout> SHOUT_NAME_CODEC = Codec.STRING.flatXmap(
            name -> {
                if (ALL_SHOUTS == null || ALL_SHOUTS.isEmpty()) {
                    // Failsafe for early init
                    return DataResult.success(DUMMY_INITIAL_SHOUT);
                }
                return ALL_SHOUTS.stream()
                        .filter(shout -> shout.getName().equals(name))
                        .findFirst()
                        .map(DataResult::success)
                        .orElseGet(() -> DataResult.success(DUMMY_INITIAL_SHOUT));
            },
            shout -> DataResult.success(shout.getName())
    );

    public ShoutWord getFirstWord() {
        return this.firstWord;
    }

    public ShoutWord getSecondWord() {
        return this.secondWord;
    }

    public ShoutWord getThirdWord() {
        return this.thirdWord;
    }

    public ResourceKey<Level> dimension() {
        return this.dimension;
    }

    public ShoutWord getSpecificWord(int index) {
        if (index < 0 || index >= allWords.length) {
            throw new IndexOutOfBoundsException("Index out of bounds for shout words");
        }
        return allWords[index];
    }

    public String getName() {
        return this.name;
    }

    public final String getDescription() {
        return this.description;
    }

    public final ResourceLocation getIconLocation() {
        return this.iconLocation;
    }

    public int getUnlockedWordsCount(List<ShoutWord> shoutWordList) {
        int count = 0;
        if (shoutWordList.contains(getFirstWord())) {
            count++;
        }
        if (shoutWordList.contains(getSecondWord())) {
            count++;
        }
        if (shoutWordList.contains(getThirdWord())) {
            count++;
        }
        return count;
    }

    /// Abstract method to be implemented by subclasses.
    ///
    /// @param player    the player using the shout
    /// @param level     the level in which the shout is used
    /// @param wordsUsed the number of words used in the shout
    /// @implSpec Keep in mind that this method is called on the server side.
    /// As such, any Shouts that implement effects which must be handled client-side require that their implementation
    /// of this method sends a custom S2C packet, and the client must handle the effects. <br>For an example, see {@link WhirlwindSprintShout}
    public abstract void useShout(Player player, Level level, int wordsUsed);

    public ShoutWord tryUnlockWord(List<ShoutWord> shoutWordList) {
        if (!shoutWordList.contains(getFirstWord())) {
            return getFirstWord();
        }
        if (!shoutWordList.contains(getSecondWord())) {
            return getSecondWord();
        }
        if (!shoutWordList.contains(getThirdWord())) {
            return getThirdWord();
        }
        return null;
    }

}

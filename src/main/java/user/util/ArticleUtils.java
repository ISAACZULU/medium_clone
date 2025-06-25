package user.util;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.regex.Pattern;

@Component
public class ArticleUtils {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern EDGESDHASHES = Pattern.compile("(^-|-$)");
    
    // Average reading speed (words per minute)
    private static final int WORDS_PER_MINUTE = 200;
    
    // Additional time for images, code blocks, etc.
    private static final int IMAGE_READ_TIME = 12; // seconds per image
    private static final int CODE_BLOCK_READ_TIME = 30; // seconds per code block

    /**
     * Generate a URL-friendly slug from a title
     */
    public static String generateSlug(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "";
        }

        String normalized = Normalizer.normalize(title, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        slug = WHITESPACE.matcher(slug).replaceAll("-");
        slug = EDGESDHASHES.matcher(slug).replaceAll("");
        
        return slug.toLowerCase();
    }

    /**
     * Generate a unique slug by appending a number if the base slug already exists
     */
    public static String generateUniqueSlug(String baseSlug, java.util.function.Function<String, Boolean> slugExists) {
        String slug = baseSlug;
        int counter = 1;
        
        while (slugExists.apply(slug)) {
            slug = baseSlug + "-" + counter++;
        }
        
        return slug;
    }

    /**
     * Calculate estimated reading time in minutes
     * Enhanced calculation that considers:
     * - Word count
     * - Images (12 seconds each)
     * - Code blocks (30 seconds each)
     * - Headers and formatting
     */
    public static int calculateReadTime(String content) {
        if (content == null || content.trim().isEmpty()) {
            return 1;
        }

        // Count words
        String[] words = content.split("\\s+");
        int wordCount = words.length;
        
        // Calculate base reading time
        int baseTimeMinutes = Math.max(1, wordCount / WORDS_PER_MINUTE);
        
        // Count images (markdown image syntax: ![alt](url))
        int imageCount = countOccurrences(content, "![");
        
        // Count code blocks (markdown code blocks: ```)
        int codeBlockCount = countOccurrences(content, "```") / 2; // Each code block has opening and closing ```
        
        // Count headers (markdown headers: # ## ###)
        int headerCount = countOccurrences(content, "\n#") + countOccurrences(content, "\n##") + countOccurrences(content, "\n###");
        
        // Calculate additional time
        int additionalSeconds = (imageCount * IMAGE_READ_TIME) + 
                               (codeBlockCount * CODE_BLOCK_READ_TIME) + 
                               (headerCount * 3); // 3 seconds per header for processing
        
        int totalMinutes = baseTimeMinutes + (additionalSeconds / 60);
        
        return Math.max(1, totalMinutes);
    }

    /**
     * Calculate reading time for different content types
     */
    public static int calculateReadTimeForContent(String content, String contentType) {
        int baseTime = calculateReadTime(content);
        
        switch (contentType.toLowerCase()) {
            case "technical":
            case "tutorial":
                return (int) (baseTime * 1.5); // Technical content takes longer
            case "news":
            case "blog":
                return baseTime;
            case "research":
            case "academic":
                return (int) (baseTime * 2.0); // Research content takes much longer
            default:
                return baseTime;
        }
    }

    /**
     * Extract a summary from content (first few sentences)
     */
    public static String extractSummary(String content, int maxLength) {
        if (content == null || content.trim().isEmpty()) {
            return "";
        }

        // Remove markdown formatting
        String plainText = content.replaceAll("\\*\\*([^*]+)\\*\\*", "$1") // Bold
                                 .replaceAll("\\*([^*]+)\\*", "$1") // Italic
                                 .replaceAll("`([^`]+)`", "$1") // Inline code
                                 .replaceAll("\\[([^\\]]+)\\]\\([^)]+\\)", "$1") // Links
                                 .replaceAll("!\\[([^\\]]*)\\]\\([^)]+\\)", "") // Images
                                 .replaceAll("^#+\\s*", "") // Headers
                                 .replaceAll("```[\\s\\S]*?```", ""); // Code blocks

        // Find the first few sentences
        String[] sentences = plainText.split("[.!?]+");
        StringBuilder summary = new StringBuilder();
        
        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (sentence.length() > 0) {
                if (summary.length() + sentence.length() + 1 <= maxLength) {
                    if (summary.length() > 0) {
                        summary.append(". ");
                    }
                    summary.append(sentence);
                } else {
                    break;
                }
            }
        }
        
        String result = summary.toString();
        if (result.length() > 0 && !result.endsWith(".")) {
            result += ".";
        }
        
        return result;
    }

    /**
     * Extract tags from content (words starting with #)
     */
    public static java.util.Set<String> extractTagsFromContent(String content) {
        java.util.Set<String> tags = new java.util.HashSet<>();
        
        if (content == null || content.trim().isEmpty()) {
            return tags;
        }

        // Find hashtags in the content
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("#(\\w+)");
        java.util.regex.Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            String tag = matcher.group(1).toLowerCase();
            if (tag.length() > 0 && tag.length() <= 50) { // Reasonable tag length
                tags.add(tag);
            }
        }
        
        return tags;
    }

    /**
     * Validate slug format
     */
    public static boolean isValidSlug(String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            return false;
        }
        
        // Slug should only contain lowercase letters, numbers, and hyphens
        // Should not start or end with hyphen
        return slug.matches("^[a-z0-9]+(-[a-z0-9]+)*$");
    }

    /**
     * Count occurrences of a substring in a string
     */
    private static int countOccurrences(String text, String substring) {
        if (text == null || substring == null) {
            return 0;
        }
        
        int count = 0;
        int lastIndex = 0;
        
        while (lastIndex != -1) {
            lastIndex = text.indexOf(substring, lastIndex);
            if (lastIndex != -1) {
                count++;
                lastIndex += substring.length();
            }
        }
        
        return count;
    }

    /**
     * Format view count for display (e.g., 1000 -> 1K, 1500000 -> 1.5M)
     */
    public static String formatViewCount(int viewCount) {
        if (viewCount < 1000) {
            return String.valueOf(viewCount);
        } else if (viewCount < 1000000) {
            return String.format("%.1fK", viewCount / 1000.0);
        } else {
            return String.format("%.1fM", viewCount / 1000000.0);
        }
    }

    /**
     * Calculate engagement rate based on views and other metrics
     */
    public static double calculateEngagementRate(int views, int likes, int comments, int shares) {
        if (views == 0) {
            return 0.0;
        }
        
        // Weighted engagement calculation
        double engagement = (likes * 1.0) + (comments * 2.0) + (shares * 3.0);
        return (engagement / views) * 100; // Return as percentage
    }
} 
public class Main {
    public static void main(String[] args) {
        String text = "凭4-2-0329到";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\b\\d{1,2}-\\d{1,2}-\\d{3,4}\\b");
        java.util.regex.Matcher m = p.matcher(text);
        if (m.find()) {
            System.out.println("Match: " + m.group());
        } else {
            System.out.println("No match");
        }
    }
}

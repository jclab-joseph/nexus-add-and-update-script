package kr.jclab.nexus.provisioner.addandupdatescript;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import javax.ws.rs.NotFoundException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.sonatype.nexus.script.ScriptClient;
import org.sonatype.nexus.script.ScriptXO;

public class AddUpdateScript {
    public static void main(String[] args) {
        Options options = new Options();
        // options.usage("java kr.jclab.nexus.provisioner.addandupdatescript.AddUpdateScript -u admin -p admin123 -f scriptFile.groovy [-n explicitName] [-h nx3Url]");
        options.addOption("u", "username", true, "A User with permission to use the NX3 Script resource");
        options.addOption("p", "password", true, "Password for given User");
        options.addOption("f", "file", true, "Script file to send to NX3");
        options.addOption("h", "host", true, "NX3 host url (including port if necessary). Defaults to http://localhost:8081");
        options.addOption("n", "name", true, "Name to store Script file under. Defaults to the name of the Script file.");

        DefaultParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("Error parsing command line arguments: " + e.getMessage());
            return;
        }

        String username = cmd.getOptionValue("username");
        String password = cmd.getOptionValue("password");
        String filePath = cmd.getOptionValue("file");
        String host = cmd.getOptionValue("host", "http://localhost:8081");
        String name = cmd.getOptionValue("name");

        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("File not found: " + filePath);
            return;
        }

        ScriptClient scripts = new ResteasyClientBuilder().build()
                .register(new BasicAuthentication(username, password))
                .target(host + "/service/rest")
                .proxy(ScriptClient.class);

        boolean newScript = true;
        try {
            scripts.read(name != null ? name : file.getName());
            newScript = false;
            System.out.println("Existing Script named " + (name != null ? name : file.getName()) + " will be updated");
        } catch (NotFoundException e) {
            System.out.println("Script named " + (name != null ? name : file.getName()) + " will be created");
        }

        try {
            ScriptXO script = new ScriptXO(name != null ? name : file.getName(), FileUtils.readFileToString(file, StandardCharsets.UTF_8), "groovy");
            if (newScript) {
                scripts.add(script);
            } else {
                scripts.edit(name != null ? name : file.getName(), script);
            }
            System.out.println("Stored scripts are now: " + scripts.browse().stream().map(ScriptXO::getName).collect(Collectors.toList()));
        } catch (IOException e) {
            System.err.println("Error reading script file: " + e.getMessage());
        }
    }
}
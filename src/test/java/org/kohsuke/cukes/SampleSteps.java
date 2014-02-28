package org.kohsuke.cukes;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

/**
 * Let's see if this class gets indexed.
 *
 * @author Kohsuke Kawaguchi
 */
public class SampleSteps {
    @Given("^I eat (.+)$")
    public void eat(String name) {
    }

    @Then("^I feel good$")
    public void feelingGreat() {
    }
}
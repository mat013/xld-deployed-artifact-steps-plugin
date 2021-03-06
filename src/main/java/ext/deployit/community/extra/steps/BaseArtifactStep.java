/**
 * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS
 * FOR A PARTICULAR PURPOSE. THIS CODE AND INFORMATION ARE NOT SUPPORTED BY XEBIALABS.
 */
package ext.deployit.community.extra.steps;

import com.xebialabs.deployit.plugin.api.deployment.specification.Delta;
import com.xebialabs.deployit.plugin.api.flow.Step;
import com.xebialabs.deployit.plugin.api.rules.Scope;
import com.xebialabs.deployit.plugin.api.rules.StepMetadata;
import com.xebialabs.deployit.plugin.api.rules.StepParameter;
import com.xebialabs.deployit.plugin.api.rules.StepPostConstructContext;
import com.xebialabs.deployit.plugin.api.udm.Container;
import com.xebialabs.deployit.plugin.api.udm.Deployed;
import com.xebialabs.deployit.plugin.overthere.Host;
import com.xebialabs.deployit.plugin.overthere.HostContainer;
import com.xebialabs.overthere.OverthereFile;


public abstract class BaseArtifactStep implements Step {

    @StepParameter(name = "target-host", description = "A target host where the step is applied.", calculated = true)
    private Host targetHost;

    @StepParameter(name = "targetPath", description = "Path of the file or folder where the artifact has been uploaded.")
    private String targetPath;

    @StepParameter(name = "order", description = "The execution order of this step", calculated = true)
    int order = 0;

    @StepParameter(name = "description", description = "The description of this step", calculated = true)
    String description = "";

    @StepParameter(name = "sharedTarget", description = "Tell the target directory is shared", calculated = true)
    private boolean sharedTarget = true;

    public int getOrder() {
        return order;
    }

    public String getDescription() {
        return description;
    }

    protected void doConfigure(final StepPostConstructContext ctx) {
        if (ctx.getScope() != Scope.DEPLOYED) {
            final StepMetadata annotation = this.getClass().getAnnotation(StepMetadata.class);
            throw new RuntimeException("<" + annotation.name() + ">step can be used only using the 'deployed' scope");
        }

        if (targetHost == null) {
            targetHost = defaultHost(ctx);
        }
    }

    public Host getTargetHost() {
        return targetHost;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public boolean isSharedTarget() {
        return sharedTarget;
    }

    private Host defaultHost(final StepPostConstructContext context) {
        final Container container = getDeployedOrPrevious(context.getDelta()).getContainer();
        if (container instanceof HostContainer) {
            HostContainer hostContainer = (HostContainer) container;
            return hostContainer.getHost();
        } else if (container.hasProperty("host")) {
            return container.getProperty("host");
        }
        return null;
    }

    protected Deployed<?, ?> getDeployedOrPrevious(final Delta delta) {
        switch (delta.getOperation()) {
            case CREATE:
            case MODIFY:
            case NOOP:
                return delta.getDeployed();
            case DESTROY:
                return delta.getPrevious();
        }
        throw new RuntimeException("Should not be here !" + delta);
    }


    protected String stringPathPrefix(final OverthereFile file, final String prefix) {
        final String path = file.getPath();
        final int path_length = path.length();
        final int prefix_length = prefix.length();
        final String relativePath = path.substring(prefix_length + 1, path_length);
        return relativePath.replace('\\', '/');
    }

}

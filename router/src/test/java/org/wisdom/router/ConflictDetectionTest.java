package org.wisdom.router;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.RouteBuilder;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Test the conflict detection.
 */
public class ConflictDetectionTest {

    RequestRouter router = new RequestRouter();

    @Test
    public void sameRouteInTwoControllers() throws Exception {
        FakeController controller1 = new FakeController();
        controller1.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo")
        ));
        router.bindController(controller1);

        FakeController controller2 = new FakeController();
        controller2.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller2, "foo")
        ));
        router.bindController(controller2);

        // Retrieve route
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo").getControllerObject()).isEqualTo(controller1);
    }

    @Test
    public void sameRouteInSameControllers() throws Exception {
        FakeController controller1 = new FakeController();
        controller1.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo"),
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo")
        ));
        router.bindController(controller1);

        // Retrieve route
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo")).isNull();
    }

    @Test
    public void sameRouteInSameControllersUsingAnnotations() throws Exception {
        FakeController controller1 = new FakeController() {
            @org.wisdom.api.annotations.Route(method = HttpMethod.GET, uri = "/foo")
            public Result index() {
                return ok();
            }

            @org.wisdom.api.annotations.Route(method = HttpMethod.GET, uri = "/foo")
            public Result index2() {
                return ok();
            }
        };
        router.bindController(controller1);
        // Retrieve route
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo")).isNull();
    }

    @Test
    public void sameRouteInSameControllersUsingAnnotationsAndRoutesMethod() throws Exception {
        FakeController controller1 = new FakeController() {
            @org.wisdom.api.annotations.Route(method = HttpMethod.GET, uri = "/foo")
            public Result index() {
                return ok();
            }
        };
        controller1.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo")));
        router.bindController(controller1);
    }

    @Test
    public void noConflictOnDifferentMethod() throws Exception {
        FakeController controller1 = new FakeController();
        controller1.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo"),
                new RouteBuilder().route(HttpMethod.DELETE).on("/foo").to(controller1, "foo")
        ));
        router.bindController(controller1);
    }

    @Test
    public void ensureThatAllRoutesAreRemovedOnConflicts() throws Exception {
        FakeController controller1 = new FakeController();
        controller1.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/bar").to(controller1, "foo"),
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo"),
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo")
        ));

        router.bindController(controller1);

        assertThat(router.getRouteFor(HttpMethod.GET, "/bar")).isNull();
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo")).isNull();
    }

}

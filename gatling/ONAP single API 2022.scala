import io.gatling.core.Predef.{exec, _}
import io.gatling.http.Predef._
import _root_.io.gatling.core.scenario.Simulation
import java.util.UUID
import scala.concurrent.duration._

import scala.util.Random

class ONAP_2204_SingleAPI extends Simulation {

  val httpHeaders = Map(
    "Accept" -> """application/json""",
    "Content-Type"-> """application/json""",
    "X-FromAppId"-> """ONAP-fromappId""",
    "X-TransactionId"-> """ONAP-transId""",
    "Connection"-> """Keep-Alive"""
  )

  def idGenerator() = "random-" + UUID.randomUUID().toString;
  val feeder = Iterator.continually(Map("randVal" -> (Random.alphanumeric.take(20).mkString)))

  val tenantFeeder = csv("./src/test/gatling/bodies/onaptenants.csv").random
  val LogLinkFeeder = csv("./src/test/gatling/bodies/onaploglinks.csv").random
  val PServerFeeder = csv("./src/test/gatling/bodies/onappservers.csv").random
  val serviceInstanceFeeder = csv("./src/test/gatling/bodies/onapserviceinstance.csv").random
  val genericvnfFeeder = csv("./src/test/gatling/bodies/onapvnfs.csv").random
  val CloudRegionFeeder = csv("./src/test/gatling/bodies/onapcloudregions.csv").random
  val ComplexFeeder = csv("./src/test/gatling/bodies/onapcomplexes.csv").random
  val customerFeeder = csv("./src/test/gatling/bodies/onapcustomers.csv").random
  val vfmoduleFeeder = csv("./src/test/gatling/bodies/onapvf-modules.csv").random
  val pnfFeeder = csv("./src/test/gatling/bodies/onappnfs.csv").random





  val host   = System.getProperty("TARGET_HOST", "Enter Host with Port");

  val httpBuilder = http.baseURL(host);

  def SingleAPITest(env: String) = scenario("Run each API single-user " + env)

  /***
    *
    * @param env
    * @PUT tenant
    */
.repeat(3){
       exec(session => {
        session.set("stenant", idGenerator())
      })
      .exec(
        http("PutTenant " + env)
        .put("/aai/v23/cloud-infrastructure/cloud-regions/cloud-region/onap-CROwner4/onap-CRId4/tenants/tenant/${stenant}")
        .body(ElFileBody("newtenant_valid.json"))
        .headers(httpHeaders)
        .check(
          status.find.in(201)
        )
      )
      .pause(1,1)
      .exec(
        http("GetTenant " + env)
        .get("/aai/v23/cloud-infrastructure/cloud-regions/cloud-region/onap-CROwner4/onap-CRId4/tenants/tenant/${stenant}")
        .headers(httpHeaders)
        .check(
          status.find.in(200)
        )
        .check(jsonPath("$.resource-version").saveAs("resourcever"))
      )
      .exec(
        http("DeleteTenant " + env)
        .delete("/aai/v23/cloud-infrastructure/cloud-regions/cloud-region/onap-CROwner4/onap-CRId4/tenants/tenant/${stenant}?resource-version=${resourcever}")
        .headers(httpHeaders)
        .check(status.find.in(204))
      )

   }

   /***
    *
    * @param env
    * @patch tenant
    */
.repeat(3){
 // Get the resource version before doing the PATCH
      feed(tenantFeeder)
      .exec(
        http("GetTenant " + env)
        .get("/aai/v23/cloud-infrastructure/cloud-regions/cloud-region/onap-CROwner/onap-CRId/tenants/tenant/onap-tenant-patch")
        .headers(httpHeaders)
        .check(
          status.find.in(200)
        )
        .check(
          jsonPath("$.resource-version").saveAs("rsrcver")
          )
      )

      .exec(
        http("PatchTenant " + env)
        .patch("/aai/v23/cloud-infrastructure/cloud-regions/cloud-region/onap-CROwner/onap-CRId/tenants/tenant/onap-tenant-patch?resource-version=${rsrcver}")
        .headers(httpHeaders)
        .headers(Map(
          "Content-Type"-> """application/merge-patch+json"""
        ))
        .body(ElFileBody("patch-tenant.json"))
        .check(
          status.find.in(200)
        )
      )
      .pause(1,1)
    }

           /***
    *
    * @param env
    * @get tenant depth=0
    */
.repeat(3){
     feed(tenantFeeder)
      .exec(
        http("GetTenantdepth0 " + env)
        .get("/aai/v23/cloud-infrastructure/cloud-regions/cloud-region/onap-CROwner3/onap-CRId3/tenants/tenant/onap-tenantId3?depth=0")
        .headers(httpHeaders)
        .check(
          status.find.in(200)
        )
      )
       .pause(1,1)
    }

                   /***
    *
    * @param env
    * @get vserver-depth=all
    */
   .repeat(3){
       exec(
          http("GetVserverdepthall " + env)
          .get("/aai/v23/cloud-infrastructure/cloud-regions/cloud-region/onap-CROwner2/onap-CRId2/tenants/tenant/onap-tenantId2/vservers/vserver/onap-vserverId2?depth=all")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
       .pause(1,1)
    }

                           /***
    *
    * @param env
    * @get vserver
    */
   .repeat(3){
         exec(
          http("GetVserver " + env)
          .get("/aai/v23/cloud-infrastructure/cloud-regions/cloud-region/onap-CROwner2/onap-CRId2/tenants/tenant/onap-tenantId2/vservers/vserver/onap-vserverId2")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
       .pause(1,1)
    }

                                   /***
    *
    * @param env
    * @get gen-vnf
    */
   .repeat(3){
        feed(genericvnfFeeder)
        .exec(
          http("GetVnf " + env)
          .get("/aai/v23/network/generic-vnfs/generic-vnf/${vnf-id}")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
       .pause(1,1)
    }

                                           /***
    *
    * @param env
    * @get gen-vnf depth=1
    */
   .repeat(3){
        feed(genericvnfFeeder)
        .exec(
          http("GetVnfdepth1 " + env)
          .get("/aai/v23/network/generic-vnfs/generic-vnf/${vnf-id}?depth=1")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
       .pause(1,1)
    }

                                                   /***
    *
    * @param env
    * @get vf-module depth=1
    */
   .repeat(3){
        feed(vfmoduleFeeder)
        .exec(
          http("GetVfmoduledepthl " + env)
          .get("/aai/v23/network/generic-vnfs/generic-vnf/onap-genericvnfId/vf-modules/vf-module/onap-vnfmoduleId?depth=1")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
       .pause(1,1)
    }

                                                           /***
    *
    * @param env
    * @put vf-module
    */
   .repeat(3){
        feed(genericvnfFeeder)
      exec(session => {
        session.set("svfmodule", idGenerator())
      })
      .exec(
        http("PutVfmodule " + env)
        .put("/aai/v23/network/generic-vnfs/generic-vnf/onap-genericvnfId/vf-modules/vf-module/${svfmodule}")
        .body(ElFileBody("newvf-module_valid.json"))
        .headers(httpHeaders)
        .check(
          status.find.in(201)
        )
      )
      .pause(1,1)
      .exec(
        http("GetVfmodule " + env)
        .get("/aai/v23/network/generic-vnfs/generic-vnf/onap-genericvnfId/vf-modules/vf-module/${svfmodule}")
        .headers(httpHeaders)
        .check(
          status.find.in(200)
        )
        .check(jsonPath("$.resource-version").saveAs("resourcever"))
      )
.pause(1,1)
      .exec(
        http("DeleteVfmodule " + env)
        .delete("/aai/v23/network/generic-vnfs/generic-vnf/onap-genericvnfId/vf-modules/vf-module/${svfmodule}?resource-version=${resourcever}")
        .headers(httpHeaders)
        .check(status.find.in(204))
      )
.pause(1,1)

    }

                                                                   /***
    *
    * @param env
    * @patch/post vf-module
    */
   .repeat(3){
 // Get the resource version before doing the PATCH
      feed(vfmoduleFeeder)
      .exec(
        http("GetVfmodule " + env)
        .get("/aai/v23/network/generic-vnfs/generic-vnf/onap-genericvnfId/vf-modules/vf-module/onap-vnfmoduleId")
        .headers(httpHeaders)
        .check(
          status.find.in(200)
        )
        .check(
          jsonPath("$.resource-version").saveAs("rsrcver")
          )
      )

      .exec(
        http("PatchVfmodule " + env)
        .patch("/aai/v23/network/generic-vnfs/generic-vnf/onap-genericvnfId/vf-modules/vf-module/onap-vnfmoduleId?resource-version=${rsrcver}")
        .headers(httpHeaders)
        .headers(Map(
          "Content-Type"-> """application/merge-patch+json"""
        ))
        .body(ElFileBody("patch-vf-module.json"))
        .check(
          status.find.in(200)
        )
      )

    }

   /***
    *
    * @param env
    * @get recents api vlan
    */
   .repeat(3){
          exec(
          http("GetRecentsAPI Vlan " + env)
          .get("/aai/recents/v23/vlan?hours=192")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
       .pause(1,1)
    }

        /***
    *
    * @param env
    * @get recents api l-interface
    */
   .repeat(3){
          exec(
          http("GetRecentsAPI l-interface " + env)
          .get("/aai/recents/v23/l-interface?hours=192")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
       .pause(1,1)
    }

    /***
    *
    * @param env
    * @get recents api vserver
    */
   .repeat(3){
          exec(
          http("GetRecentsAPI vserver " + env)
          .get("/aai/recents/v23/vserver?hours=192")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
       .pause(1,1)
    }

            /***
    *
    * @param env
    * @get all CRs1
    */
   .repeat(3){
          exec(
          http("GetallCR onap-CRId " + env)
          .get("/aai/v23/cloud-infrastructure/cloud-regions/?cloud-region-id=onap-CRId")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
       .pause(1,1)
    }

                    /***
    *
    * @param env
    * @get all CRs2
    */
   .repeat(3){
          exec(
          http("GetallCR onap-CRId " + env)
          .get("/aai/v23/cloud-infrastructure/cloud-regions/?cloud-region-id=onap-CRId")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
       .pause(1,1)
    }

                            /***
    *
    * @param env
    * @get all services
    */
   .repeat(3){
          exec(
          http("GetallServices " + env)
          .get("/aai/v23/service-design-and-creation/services")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
       .pause(1,1)
    }

                                    /***
    *
    * @param env
    * @get all CRs
    */
   .repeat(3){
          exec(
          http("GetallCRs " + env)
          .get("/aai/v23/cloud-infrastructure/cloud-regions/?cloud-region-id=onap-CRId")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
       .pause(1,1)
    }

                                            /***
    *
    * @param env
    * @get nodes-query
    */
   .repeat(3){
          exec(
          http("Getnodes-query VNF " + env)
          .get("/aai/v23/search/nodes-query?search-node-type=generic-vnf&filter=vnf-name:EQUALS:onap-genericvnfname")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
       .pause(1,1)
    }

                                                    /***
    *
    * @param env
    * @PUT singletx1
    */
    .feed(tenantFeeder)
        .repeat(3){
      exec(session => {
        session.set("sPServer", idGenerator())
      })
      .exec(session => {
        session.set("sVServer", idGenerator())
      })
      .exec(
        http("BulkSingleTx1 " + env)
        .post("/aai/v22/bulk/single-transaction")
        .body(ElFileBody("onapbulk-single-tx_putPServerVServer.json"))
        .headers(httpHeaders)
        .check(
          status.find.in(201)
        )
      )
        .pause(2,33)
        .exec(
          http("GetVserver " + env)
          .get("/aai/v22/cloud-infrastructure/cloud-regions/cloud-region/onap-CROwner/onap-CRId/tenants/tenant/onap-tenantId1/vservers/vserver/${sVServer}?depth=0&nodes-only=true")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
          .check(jsonPath("$.resource-version").saveAs("rsrcver"))
        )
        .exec(
         http("DeleteVserver " + env)
          .delete("/aai/v22/cloud-infrastructure/cloud-regions/cloud-region/onap-CROwner/onap-CRId/tenants/tenant/onap-tenantId1/vservers/vserver/${sVServer}?resource-version=${rsrcver}")
          .headers(httpHeaders)

          .check(status.find.in(204))
        )
        .exec(
          http("GetPserver " + env)
          .get("/aai/v22/cloud-infrastructure/pservers/pserver/${sPServer}?depth=0&nodes-only=true")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
          .check(jsonPath("$.resource-version").saveAs("rsrcver"))
        )
        .exec(
         http("DeletePserver " + env)
          .delete("/aai/v22/cloud-infrastructure/pservers/pserver/${sPServer}?resource-version=${rsrcver}")
          .headers(httpHeaders)
          .check(status.find.in(204))
        )
       .pause(1,1)
    }


                                                            /***
    *
    * @param env
    * @PUT bulksingletx2
    */
   .repeat(3){
     exec(session => {
        session.set("CR1", idGenerator())
      })
      .exec(session => {
        session.set("AZ1", idGenerator())
      })
                .exec(session => {
        session.set("flavor1", idGenerator())
      })
                .exec(session => {
        session.set("image1", idGenerator())
      })
                .exec(session => {
        session.set("complex1", idGenerator())
      })
                .exec(session => {
        session.set("pserver1", idGenerator())
      })
                .exec(session => {
        session.set("pserver2", idGenerator())
      })
                .exec(session => {
        session.set("pserver3", idGenerator())
      })
      .exec(
        http("BulkSingleTx2 " + env)
        .post("/aai/v21/bulk/single-transaction")
        .body(ElFileBody("onap-patch-bulk-single-tx.json"))
        .headers(httpHeaders)
        .check(
          status.find.in(201)
        )
      )
       .pause(1,1)
    }

                                                                    /***
    *
    * @param env
    * @PUT bulksingletx3
    */
    .feed(tenantFeeder)
        .repeat(3){
      exec(session => {
        session.set("sPServer", idGenerator())
      })
      .exec(session => {
        session.set("sVServer", idGenerator())
      })
      .exec(
        http("BulkSingleTx3 " + env)
        .post("/aai/v22/bulk/single-transaction")
        .body(ElFileBody("onapbulk-single-tx_putPServerVServer.json"))
        .headers(httpHeaders)
        .check(
          status.find.in(201)
        )
      )
        .pause(2,5)
        .exec(
          http("GetVserver " + env)
          .get("/aai/v22/cloud-infrastructure/cloud-regions/cloud-region/onap-CROwner/onap-CRId/tenants/tenant/onap-tenantId1/vservers/vserver/${sVServer}?depth=0&nodes-only=true")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
          .check(jsonPath("$.resource-version").saveAs("rsrcver"))
        )
        .exec(
         http("DeleteVserver " + env)
          .delete("/aai/v22/cloud-infrastructure/cloud-regions/cloud-region/onap-CROwner/onap-CRId/tenants/tenant/onap-tenantId1/vservers/vserver/${sVServer}?resource-version=${rsrcver}")
          .headers(httpHeaders)

          .check(status.find.in(204))
        )
        .exec(
          http("GetPserver " + env)
          .get("/aai/v22/cloud-infrastructure/pservers/pserver/${sPServer}?depth=0&nodes-only=true")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
          .check(jsonPath("$.resource-version").saveAs("rsrcver"))
        )
        .exec(
         http("DeletePserver " + env)
          .delete("/aai/v22/cloud-infrastructure/pservers/pserver/${sPServer}?resource-version=${rsrcver}")
          .headers(httpHeaders)
          .check(status.find.in(204))
        )
       .pause(1,1)
    }

        /***
    *
    * @param env
    * @CQ pserverzdeviceresurldepth1
    */
   .repeat(3){
       exec(
          http("CQ pserverzdeviceresurldepth1 " + env)
          .put("/aai/v22/query?format=resource_and_url&depth=1")
          .headers(httpHeaders)
          .body(ElFileBody("onap-CQ-pserver-zdevice.json"))
          .check(
            status.find.in(200)
          )
        )
       .pause(1,1)
    }

        /***
    *
    * @param env
    * @CQ pserverzdeviceres
    */
   .repeat(3){
        exec(
          http("CQ pserverzdeviceres " + env)
          .put("/aai/v22/query?format=resource")
          .headers(httpHeaders)
          .body(ElFileBody("onap-CQ-pserver-zdevice.json"))
          .check(
            status.find.in(200)
          )
        )
       .pause(1,1)
    }

                /***
    *
    * @param env
    * @CQ vlanrangefromVlantag
    */
   .repeat(3){
       exec(
          http("CQ vlanrangefromVlantag " + env)
          .put("/aai/v22/query?format=resource")
          .headers(httpHeaders)
          .body(ElFileBody("onap-CQ-vlanrange-fromVlantag.json"))
          .check(
            status.find.in(200)
          )
        )
       .pause(1,1)
    }

                        /***
    *
    * @param env
    * @CQ pserverzdeviceres
    */
   .repeat(3){
     exec(
          http("CQ getClfiRoadmTailSummary " + env)
          .put("/aai/v22/query?format=simple&nodesOnly=true")
          .headers(httpHeaders)
          .body(ElFileBody("onap-CQ-getClfiRoadmTailSummary.json"))
          .check(
            status.find.in(200)
          )
        )
       .pause(1,1)
    }

                                /***
    *
    * @param env
    * @DSL Query1
    */
   .repeat(3){
      exec(
        http("DSL Query1 PUT")
        .put("/aai/v21/dsl?format=resource")
        .headers(httpHeaders)
        .body(ElFileBody("onap-DSL1.json"))
        .check(
          status.find.in(200)
        )
      )
       .pause(1,1)
    }

                                        /***
    *
    * @param env
    * @DSL Query2
    */
   .repeat(3){
        exec(
        http("DSL Query2 PUT")
        .put("/aai/v21/dsl?format=resource")
        .headers(httpHeaders)
        .body(ElFileBody("onap-DSL2.json"))
        .check(
          status.find.in(200)
        )
      )
       .pause(1,1)
    }

                                                /***
    *
    * @param env
    * @DSL Query3
    */
   .repeat(3){
       exec(
        http("DSL Query3 PUT")
        .put("/aai/v21/dsl?format=resource")
        .headers(httpHeaders)
        .body(ElFileBody("onap-DSL3.json"))
        .check(
          status.find.in(200)
        )
      )
       .pause(1,1)
    }

                                                /***
    *
    * @param env
    * @DSL Query4
    */
   .repeat(3){
       exec(
        http("DSL Query4 PUT")
        .put("/aai/v21/dsl?format=resource")
        .headers(httpHeaders)
        .body(ElFileBody("onap-DSL4.json"))
        .check(
          status.find.in(200)
        )
      )
       .pause(1,1)
    }

                                                        /***
    *
    * @param env
    * @2min timeout
    */
  .repeat(3){
          exec(
          http("GetallCust " + env)
          .get("/aai/v23/business/customers")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
       .pause(1,1)
    }

  setUp(
      SingleAPITest("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port"))
  )
  .maxDuration(10 minutes)
}
import io.gatling.core.Predef.{exec, _}
import io.gatling.http.Predef._
import _root_.io.gatling.core.scenario.Simulation
import java.util.UUID
import scala.concurrent.duration._

import scala.util.Random

class ONAP_2204_Baseline extends Simulation {
  val httpHeaders = Map(
    "Accept" -> """application/json""",
    "Content-Type"-> """application/json""",
      "X-FromAppId"-> """ONAP-fromappId""",
    "X-TransactionId"-> """ONAP-transId""",
    "Connection"-> """Keep-Alive"""
  )

  def idGenerator() = "random-" + UUID.randomUUID().toString;
  val r = scala.util.Random
  val feeder = Iterator.continually(Map("randVal" -> (Random.alphanumeric.take(20).mkString)))

  val PServerFeeder = csv("./src/test/gatling/bodies/onappservers.csv").random
  val serviceInstanceFeeder = csv("./src/test/gatling/bodies/onapserviceinstance.csv").random
  val genericvnfFeeder = csv("./src/test/gatling/bodies/onapvnfs.csv").random
  val customerFeeder = csv("./src/test/gatling/bodies/onapcustomers.csv").random
  val vnfcFeeder = csv("./src/test/gatling/bodies/onapvnfcs.csv").random
  val platformFeeder = csv("./src/test/gatling/bodies/onapplatform.csv").random
  val pnfFeeder = csv("./src/test/gatling/bodies/onappnfs.csv").random
  val configurationFeeder = csv("./src/test/gatling/bodies/onapconfigurations.csv").random
  val owningentityFeeder = csv("./src/test/gatling/bodies/onapowning_entity.csv").random
  val projectFeeder = csv("./src/test/gatling/bodies/onapproject.csv").random
  val lobFeeder = csv("./src/test/gatling/bodies/onapLOB.csv").random

  val host = System.getProperty("TARGET_HOST", "Enter Host with Port");
  val url = "Enter host without port"

  val httpBuilder = http.baseURL(host);


  /***
    *
    * @param env
    * Sends a PUT tenant request
    */
  def PutTenant(env: String) = scenario("Put Tenant " + env)
    .forever(){
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
      .pause(2,13)
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
        http("DelTenant " + env)
        .delete("/aai/v23/cloud-infrastructure/cloud-regions/cloud-region/onap-CROwner4/onap-CRId4/tenants/tenant/${stenant}?resource-version=${resourcever}")
        .headers(httpHeaders)
        .check(status.find.in(204))
      )
      .pause(1,1)
   }

     /***
    *
    * @param env
    * Sends a PATCH tenant request
    */
  def PatchTenant(env: String) = scenario("Patch Tenant " + env)
    .forever(){
// Get the resource version before doing the PATCH
      exec(
        http("Get Tenant " + env)
        .get("/aai/v23/cloud-infrastructure/cloud-regions/cloud-region/onap-CROwner/onap-CRId/tenants/tenant/onap-tenant-patch")
        .headers(httpHeaders)
        .check(
          status.find.in(200)
        )
        .check(
          jsonPath("$.resource-version").saveAs("rsrcver")
          )
      )
      .pause(1,1)

      .exec(
        http("Patch Tenant " + env)
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
      .pause(2,5)
    }

 /***
    *
    * @param env
    * @return Tenant Get depth0
    */
  def GetTenant(env: String) = scenario("Get Tenantdepth0 " + env)
    .forever(){
      exec(
        http("Get Tenantdepth0 " + env)
        .get("/aai/v23/cloud-infrastructure/cloud-regions/cloud-region/onap-CROwner3/onap-CRId3/tenants/tenant/onap-tenantId3?depth=0")
        .headers(httpHeaders)
        .check(
          status.find.in(200)
        )
      )
      .pause(2,6)
}

  /***
    *
    * @param env
    * Sends a DELETE tenant request
    */
  def DeleteTenant(env: String) = scenario("Delete Tenant " + env)
    .forever(){
      exec(session => {
        session.set("stenant", idGenerator())
      })
      .exec(
        http("PutTenant " + env)
        .put("/aai/v23/cloud-infrastructure/cloud-regions/cloud-region/onap-CROwner5/onap-CRId5/tenants/tenant/${stenant}")
        .body(ElFileBody("newtenant_valid.json"))
        .headers(httpHeaders)
        .check(
          status.find.in(201)
        )
      )
      .pause(1,1)
      .exec(
        http("GetTenant " + env)
        .get("/aai/v23/cloud-infrastructure/cloud-regions/cloud-region/onap-CROwner5/onap-CRId5/tenants/tenant/${stenant}")
        .headers(httpHeaders)
        .check(
          status.find.in(200)
        )
        .check(jsonPath("$.resource-version").saveAs("resourcever"))
      )
      .exec(
        http("DeleteTenant " + env)
        .delete("/aai/v23/cloud-infrastructure/cloud-regions/cloud-region/onap-CROwner5/onap-CRId5/tenants/tenant/${stenant}?resource-version=${resourcever}")
        .headers(httpHeaders)
        .check(status.find.in(204))
      )
      .pause(1,1)
   }

  /**
    *
    * @param env
    * @Send a GET Vserver depth=all request
    */
    def Getvserver1(env: String) = scenario("Get Vserverdepthall " + env)
      .forever(){
         exec(
          http("Get Vserverdepthall " + env)
          .get("/aai/v23/cloud-infrastructure/cloud-regions/cloud-region/onap-CROwner2/onap-CRId2/tenants/tenant/onap-tenantId2/vservers/vserver/onap-vserverId2?depth=all")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
        .pause(2,5)
      }

  /**
    *
    * @param env
    * @Send a GET VF-Module depth=1request
    */
    def GetVFModule1(env: String) = scenario("Get VF-Moduledepth1 " + env)
      .forever(){
        exec(
          http("Get VF-Moduledepthl " + env)
          .get("/aai/v23/network/generic-vnfs/generic-vnf/onap-genericvnfId/vf-modules/vf-module/onap-vnfmoduleId?depth=1")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
        .pause(2,5)
      }

  /**
    *
    * @param env
    * @Send a GET Vserver request
    */
    def Getvserver2(env: String) = scenario("Get Vserver " + env)
      .forever(){
         exec(
          http("Get Vserver " + env)
          .get("/aai/v23/cloud-infrastructure/cloud-regions/cloud-region/onap-CROwner2/onap-CRId2/tenants/tenant/onap-tenantId2/vservers/vserver/onap-vserverId2")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
        .pause(2,6)
      }

  /**
    *
    * @param env
    * @Send a GET SI format=pathed request
    */
    def GetSI1(env: String) = scenario("Get SIpathed " + env)
      .forever(){
        feed(serviceInstanceFeeder)
        .exec(
          http("Get SIpathed " + env)
          .get("/aai/v23/nodes/service-instances/service-instance/${service-instance-id}?format=pathed")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
        .pause(2,3)
      }

            /**
    *
    * @param env
    * @Send a V23 GET vnf request
    */
    def GetVNF1(env: String) = scenario("Get VNF " + env)
      .forever(){
        feed(genericvnfFeeder)
        .exec(
          http("Get VNF " + env)
          .get("/aai/v23/network/generic-vnfs/generic-vnf/${vnf-id}")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
        .pause(2,5)
      }

  /**
    *
    * @param env
    * @Send a GET vnf depth=1 request
    */
    def GetVNF2(env: String) = scenario("Get VNFdepth1 " + env)
      .forever(){
        feed(genericvnfFeeder)
        .exec(
          http("Get VNFdepth1 " + env)
          .get("/aai/v23/network/generic-vnfs/generic-vnf/${vnf-id}?depth=1")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
        .pause(2,5)
      }


            /**
    *
    * @param env
    * @Send a GET pservers depth=all request
    */
    def GetPserver1(env: String) = scenario("Get Pserverdepthall " + env)
      .forever(){
        feed(PServerFeeder)
        .exec(
          http("Get Pserverdepthall " + env)
          .get("/aai/v22/cloud-infrastructure/pservers/pserver/${hostname}?depth=all")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
        .pause(2,5)
      }

  /**
    *
    * @param env
    * @Send a GET volume-group request
    */
    def Getvolumegroup(env: String) = scenario("Get volume-group " + env)
      .forever(){
         exec(
          http("Get volume-group " + env)
          .get("/aai/v23/cloud-infrastructure/cloud-regions/cloud-region/onap-CROwner/onap-CRId/volume-groups/volume-group/onap-volumegpId")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
        .pause(2,6)
      }

    /**
    *
    * @param env
    * @Send a GET platform request
    */
    def Getplatform(env: String) = scenario("Get platformnodes-only " + env)
      .forever(){
        feed(platformFeeder)
        .exec(
          http("Get platformnodes-only " + env)
          .get("/aai/v22/business/platforms/platform/${platform-name}?nodes-only=")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
        .pause(2,6)
      }

  /**
    *
    * @param env
    * @Send a GET sriov-vf depth=all request
    */
    def Getsriovvf1(env: String) = scenario("Get sriov-vfdepthall " + env)
      .forever(){
         exec(
          http("Get sriov-vfdepthall " + env)
          .get("/aai/v23/cloud-infrastructure/cloud-regions/cloud-region/onap-CROwner/onap-CRId/tenants/tenant/onap-tenantId/vservers/vserver/onap-vserverId/l-interfaces/l-interface/onap-linterfaceName/sriov-vfs/sriov-vf/onap-PciId?depth=all")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
        .pause(2,6)
      }

    /**
    *
    * @param env
    * @Send a GET pnf request
    */
    def Getpnf1(env: String) = scenario("Get pnf " + env)
      .forever(){
        feed(pnfFeeder)
        .exec(
          http("Get pnf " + env)
          .get("/aai/v22/network/pnfs/pnf/${pnf-name}")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
        .pause(2,6)
      }

              /**
    *
    * @param env
    * @Send a vnfc depth=1 request
    */
    def Getvnfc1(env: String) = scenario("Get vnfcdepth1 " + env)
      .forever(){
        feed(vnfcFeeder)
        .exec(
          http("Get vnfcdepth1 " + env)
          .get("/aai/v22/network/vnfcs/vnfc/${vnfc-name}?depth=1")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
        .pause(2,5)
      }

  /**
    *
    * @param env
    * @Send a GET vlan depth=1 request
    */
    def Getvlan(env: String) = scenario("Get vlandepth1 " + env)
      .forever(){
         exec(
          http("Get vlandepth1 " + env)
          .get("/aai/v22/network/generic-vnfs/generic-vnf/onap-genericvnfId/l-interfaces/l-interface/onap-linterfaceId/vlans/vlan/onap-vlanInterface?depth=1")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
        .pause(2,5)
      }

  /**
    *
    * @param env
    * @Send a GET SI depth=2 request
    */
    def GetSI2(env: String) = scenario("Get SIdepth2 " + env)
      .forever(){
         exec(
          http("Get SIdepth2 " + env)
          .get("/aai/v22/business/customers/customer/onap-customerId/service-subscriptions/service-subscription/onap-subscription-ServiceType/service-instances/service-instance/onap-serviceinstanceId?depth=2")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
        .pause(2,4)
      }

            /**
    *
    * @param env
    * @Send a GET pserver  request
    */
    def GetPserver2(env: String) = scenario("Get Pserver " + env)
      .forever(){
        feed(PServerFeeder)
        .exec(
          http("Get Pserver " + env)
          .get("/aai/v22/cloud-infrastructure/pservers/pserver/${hostname}")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
        .pause(2,5)
      }

                    /**
    *
    * @param env
    * @Send a GET configuration depth=1 request
    */
    def Getconfiguration(env: String) = scenario("Get configurationdepth1 " + env)
      .forever(){
        feed(configurationFeeder)
        .exec(
          http("Get configurationdepth1 " + env)
          .get("/aai/v22/network/configurations/configuration/${configuration-id}?depth=1")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
        .pause(2,5)
      }

  /**
    *
    * @param env
    * @Send a GET service-subscription request
    */
    def Getservicesubscription(env: String) = scenario("Get service-subscription " + env)
      .forever(){
         exec(
          http("Getservice-subscription " + env)
          .get("/aai/v22/business/customers/customer/onap-customerId/service-subscriptions/service-subscription/onap-subscription-ServiceType")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
        .pause(2,5)
      }

    /**
    *
    * @param env
    * @Send a GET pnf depth=all request
    */
    def Getpnf2(env: String) = scenario("Get pnfdepthall " + env)
      .forever(){
        feed(pnfFeeder)
        .exec(
          http("Get pnfdepthall " + env)
          .get("/aai/v22/network/pnfs/pnf/${pnf-name}?depth=all")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
        .pause(2,3)
      }

    /**
    *
    * @param env
    * @Send a GET customer request
    */
    def Getcustomer(env: String) = scenario("Get customer " + env)
      .forever(){
        feed(customerFeeder)
        .exec(
          http("Get customer " + env)
          .get("/aai/v21/business/customers/customer/${global-customer-id}")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
        .pause(2,6)
      }

    /**
    *
    * @param env
    * @Send a GET owningentity node-only request
    */
    def Getowningentity(env: String) = scenario("Get owningentitynodesonly " + env)
      .forever(){
        feed(owningentityFeeder)
        .exec(
          http("Get owningentitynodesonly " + env)
          .get("/aai/v21/business/owning-entities/owning-entity/${owning-entity-id}?nodes-only=")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
        .pause(2,6)
      }

              /**
    *
    * @param env
    * @Send a GET vnfc request
    */
    def Getvnfc2(env: String) = scenario("Get vnfc " + env)
      .forever(){
        feed(vnfcFeeder)
        .exec(
          http("Get vnfc " + env)
          .get("/aai/v22/network/vnfcs/vnfc/${vnfc-name}")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
        .pause(2,6)
      }

  /**
    *
    * @param env
    * @Send a GET vlan-tag depth=1 request
    */
    def Getvlantag(env: String) = scenario("Get vlantagdepth1 " + env)
      .forever(){
         exec(
          http("Get vlantagdepth1 " + env)
          .get("/aai/v23/cloud-infrastructure/cloud-regions/cloud-region/onap-CROwner/onap-CRId/vlan-ranges/vlan-range/onap-vlanrangeId/vlan-tags?vlan-id-outer=168&depth=1")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
        .pause(2,5)
      }

  /**
    *
    * @param env
    * @Send a GET project nodes-only request
    */
    def Getproject(env: String) = scenario("Get projectnodes-only " + env)
      .forever(){
           feed(projectFeeder)
         .exec(
          http("Get projectnodes-only " + env)
          .get("/aai/v21/business/projects/project/${project-name}?nodes-only=")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
        .pause(2,6)
      }

  /**
    *
    * @param env
    * @Send a GET LOB request
    */
    def Getlob(env: String) = scenario("Get LOB " + env)
      .forever(){
           feed(lobFeeder)
         .exec(
          http("Get LOB " + env)
          .get("/aai/v21/business/lines-of-business/line-of-business/${line-of-business-name}")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
        .pause(1,3)
      }

  /**
    *
    * @param env
    * @Send a GET sriov-vf request
    */
    def Getsriovvf2(env: String) = scenario("Get sriov-vfcountIndexSize " + env)
      .forever(){
         exec(
          http("Get sriov-vfcountIndexSize " + env)
                  .get("/aai/v23/cloud-infrastructure/cloud-regions/cloud-region/onap-CROwner/onap-CRId/tenants/tenant/onap-tenantId/vservers/vserver/onap-vserverId/l-interfaces/l-interface/onap-linterfaceName/sriov-vfs/sriov-vf/onap-PciId?format=count&resultIndex=0&resultSize=1")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
        .pause(2,6)
      }

  /**
    *
    * @param env
    * @Send a GET snapshot depth=all request
    */
    def Getsnapshot(env: String) = scenario("Get snapshotdepthall " + env)
      .forever(){
         exec(
          http("Get snapshotdepthall " + env)
                  .get("/aai/v23/cloud-infrastructure/cloud-regions/cloud-region/onap-CROwner/onap-CRId/snapshots/snapshot/onapsnapshotId?depth=all")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
        .pause(2,6)
      }

  /**
    *
    * @param env
    * @Send a GET volume depth=all request
    */
    def Getvolume(env: String) = scenario("Get volumedepthall " + env)
      .forever(){
         exec(
          http("Get volumedepthall " + env)
                  .get("/aai/v23/cloud-infrastructure/cloud-regions/cloud-region/onap-CROwner/onap-CRId/tenants/tenant/onap-tenantId/vservers/vserver/onap-vserverId/volumes/volume/onap-volumeid?depth=all")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
        .pause(2,6)
      }

            /**
    *
    * @param env
    * @Send a GET CR with CRid request
    */
    def GetCR(env: String) = scenario("Get CRwithregionid " + env)
      .forever(){
         exec(
          http("Get CRwithregionid " + env)
                  .get("/aai/v22/cloud-infrastructure/cloud-regions/?cloud-region-id=onap-CRId")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
        )
        .pause(1,2)
      }

  /***
    *
    * @param env
    * Sends a PUT then del vf-module request
    */
  def PUTvfmodule(env: String) = scenario("PUT vf-module " + env)
    .forever(){
        feed(genericvnfFeeder)
      exec(session => {
        session.set("svfmodule", idGenerator())
      })
      .exec(
        http("PUT vf-module " + env)
        .put("/aai/v23/network/generic-vnfs/generic-vnf/onap-genericvnfId/vf-modules/vf-module/${svfmodule}")
        .body(ElFileBody("newvf-module_valid.json"))
        .headers(httpHeaders)
        .check(
          status.find.in(201)
        )
      )
      .pause(2,14)
      .exec(
        http("Getvfmodule " + env)
        .get("/aai/v23/network/generic-vnfs/generic-vnf/onap-genericvnfId/vf-modules/vf-module/${svfmodule}")
        .headers(httpHeaders)
        .check(
          status.find.in(200)
        )
        .check(jsonPath("$.resource-version").saveAs("resourcever"))
      )
      .exec(
        http("Deletevfmodule " + env)
        .delete("/aai/v23/network/generic-vnfs/generic-vnf/onap-genericvnfId/vf-modules/vf-module/${svfmodule}?resource-version=${resourcever}")
        .headers(httpHeaders)
        .check(status.find.in(204))
      )
      .pause(1,1)
   }

            /**
    *
    * @param env
    * @Send a GET all services request
    */
    def Getallservices(env: String) = scenario("Get all services " + env)
      .forever(){
         exec(
          http("Get all services " + env)
                  .get("/aai/v22/service-design-and-creation/services")
          .headers(httpHeaders)
.headers(Map(
          "X-FromAppId"-> """ONAP-fromappId"""
        ))
          .check(
            status.find.in(200)
          )
        )
        .pause(2,20)
      }

  /***
    *
    * @param env
    * Sends a PUT pservervserver request
    */
  def PostBulkSingleTx1(env: String) = scenario("BulkSingleTx1" + env)
    //.feed(tenantFeeder)
        .forever(){
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
        .pause(2,5)
        .exec(
          http("Vserver " + env)
          .get("/aai/v22/cloud-infrastructure/cloud-regions/cloud-region/onap-CROwner/onap-CRId/tenants/tenant/onap-tenantId1/vservers/vserver/${sVServer}?depth=0&nodes-only=true")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
          .check(jsonPath("$.resource-version").saveAs("rsrcver"))
        )
        .exec(
         http("Vserver " + env)
          .delete("/aai/v22/cloud-infrastructure/cloud-regions/cloud-region/onap-CROwner/onap-CRId/tenants/tenant/onap-tenantId1/vservers/vserver/${sVServer}?resource-version=${rsrcver}")
          .headers(httpHeaders)

          .check(status.find.in(204))
        )
        .exec(
          http("Pserver " + env)
          .get("/aai/v22/cloud-infrastructure/pservers/pserver/${sPServer}?depth=0&nodes-only=true")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
          .check(jsonPath("$.resource-version").saveAs("rsrcver"))
        )
        .exec(
         http("Pserver " + env)
          .delete("/aai/v22/cloud-infrastructure/pservers/pserver/${sPServer}?resource-version=${rsrcver}")
          .headers(httpHeaders)
          .check(status.find.in(204))
        )
      .pause(1,9)
    }

 /***
    *
    * @param env
    * Sends a PUT pservervserver2 request
    */
  def PostBulkSingleTx2(env: String) = scenario("BulkSingleTx2 " + env)
        .forever(){
      exec(session => {
        session.set("sPServer", idGenerator())
      })
      .exec(session => {
        session.set("sVServer", idGenerator())
      })
      .exec(
        http("BulkSingleTx2" + env)
        .post("/aai/v22/bulk/single-transaction")
        .body(ElFileBody("onapbulk-single-tx_putPServerVServer.json"))
        .headers(httpHeaders)
        .check(
          status.find.in(201)
        )
      )
        .pause(2,33)
        .exec(
          http("Vserver " + env)
          .get("/aai/v22/cloud-infrastructure/cloud-regions/cloud-region/onap-CROwner/onap-CRId/tenants/tenant/onap-tenantId1/vservers/vserver/${sVServer}?depth=0&nodes-only=true")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
          .check(jsonPath("$.resource-version").saveAs("rsrcver"))
        )
        .exec(
         http("Vserver  " + env)
          .delete("/aai/v22/cloud-infrastructure/cloud-regions/cloud-region/onap-CROwner/onap-CRId/tenants/tenant/onap-tenantId1/vservers/vserver/${sVServer}?resource-version=${rsrcver}")
          .headers(httpHeaders)

          .check(status.find.in(204))
        )
        .exec(
          http("Pserver " + env)
          .get("/aai/v22/cloud-infrastructure/pservers/pserver/${sPServer}?depth=0&nodes-only=true")
          .headers(httpHeaders)
          .check(
            status.find.in(200)
          )
          .check(jsonPath("$.resource-version").saveAs("rsrcver"))
        )
        .exec(
         http("Pserver " + env)
          .delete("/aai/v22/cloud-infrastructure/pservers/pserver/${sPServer}?resource-version=${rsrcver}")
          .headers(httpHeaders)
          .check(status.find.in(204))
        )
      .pause(1,9)
    }

     /***
    *
    * @param env
    * Sends a patch request
    */
        def PostBulkSingleTx3(env: String) = scenario("BulkSingleTx3 " + env)
    .forever(){
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
        http("BulkSingleTx3 " + env)
        .post("/aai/v21/bulk/single-transaction")
        .body(ElFileBody("onap-patch-bulk-single-tx.json"))
        .headers(httpHeaders)
        .check(
          status.find.in(201)
        )
      )
      .pause(2,5)
    }

  /***
    *
    * @param env
    * @CQ-pserver-zdevice1
    */
  def CQpserverzdevice1(env: String) = scenario("CQ pserverzdeviceresurldepth1 " + env)
     .forever(){
        exec(
          http("CQ pserverzdeviceresurldepth1 " + env)
          .put("/aai/v22/query?format=resource_and_url&depth=1")
          .headers(httpHeaders)
          .body(ElFileBody("onap-CQ-pserver-zdevice.json"))
          .check(
            status.find.in(200)
          )
        )
        .pause(2,4)
     }

  /***
    *
    * @param env
    * @CQ-pserver-zdevice2
    */
  def CQpserverzdevice2(env: String) = scenario("CQ pserverzdeviceres " + env)
     .forever(){
        exec(
          http("CQ pserverzdeviceres " + env)
          .put("/aai/v22/query?format=resource")
          .headers(httpHeaders)
          .body(ElFileBody("onap-CQ-pserver-zdevice.json"))
          .check(
            status.find.in(200)
          )
        )
        .pause(2,4)
     }

  /***
    *
    * @param env
    * @CQ-vlanrange-fromVlantag
    */
  def CQvlanrangefromVlantag(env: String) = scenario("CQ vlanrangefromVlantag " + env)
     .forever(){
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
    * @CQ-getClfiRoadmTailSummary
    */
  def CQgetClfiRoadmTailSummary(env: String) = scenario("CQ getClfiRoadmTailSummary " + env)
     .forever(){
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
    * @CQ-vrfs-fromVlantag
    */
  def CQvrfsfromVlantag(env: String) = scenario("CQ vrfsfromVlantag " + env)
     .forever(){
        exec(
          http("CQ vrfsfromVlantag " + env)
          .put("/aai/v22/query?format=resource")
          .headers(httpHeaders)
          .body(ElFileBody("onap-CQ-vrfs-fromVlantag.json"))
          .check(
            status.find.in(200)
          )
        )
        .pause(1,1)
     }

  /***
    *
    * @param env
    * @CQ-genericVnfs-fromPserver
    */
  def CQgenericVnfsfromPserver(env: String) = scenario("CQ genericVnfsfromPserver " + env)
     .forever(){
        exec(
          http("CQ genericVnfsfromPserver " + env)
          .put("/aai/v22/query?format=resource&depth=0")
          .headers(httpHeaders)
          .body(ElFileBody("onap-CQ-genericVnfs-fromPserver.json"))
          .check(
            status.find.in(200)
          )
        )
        .pause(2,11)
     }

  /***
    *
    * @param env
    * @CQ-zPnfs-fromPnf
    */
  def CQzPnfsfromPnf(env: String) = scenario("CQ zPnfsfromPnf " + env)
     .forever(){
        exec(
          http("CQ zPnfsfromPnf " + env)
          .put("/aai/v22/query?format=resource_and_url&depth=1")
          .headers(httpHeaders)
          .body(ElFileBody("onap-CQ-zPnfs-fromPnf.json"))
          .check(
            status.find.in(200)
          )
        )
        .pause(2,11)
     }

    /***
    *
    * @param env
    * @DSL1
    */
 def DslQuery1(env: String) = scenario("DSL Query1 " + env)
    .forever(){
      exec(
        http("DSL Query1 PUT")
        .put("/aai/v21/dsl?format=resource")
        .headers(httpHeaders)
        .body(ElFileBody("onap-DSL1.json"))
        .check(
          status.find.in(200)
        )
      )
      .pause(2,3)
    }

            /***
    *
    * @param env
    * @DSL2
    */
 def DslQuery2(env: String) = scenario("DSL Query2 " + env)
    .forever(){
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
    * @DSL3
    */
 def DslQuery3(env: String) = scenario("DSL Query3 " + env)
    .forever(){
      exec(
        http("DSL Query3 PUT")
        .put("/aai/v21/dsl?format=resource")
        .headers(httpHeaders)
        .body(ElFileBody("onap-DSL3.json"))
        .check(
          status.find.in(200)
        )
      )
      .pause(2,4)
    }

            /***
    *
    * @param env
    * @DSL4
    */
 def DslQuery4(env: String) = scenario("DSL Query4 " + env)
    .forever(){
      exec(
        http("DSL Query4 PUT")
        .put("/aai/v21/dsl?format=resource")
        .headers(httpHeaders)
        .body(ElFileBody("onap-DSL4.json"))
        .check(
          status.find.in(200)
        )
      )
      .pause(2,12)
    }

            /***
    *
    * @param env
    * @DSL5
    */
 def DslQuery5(env: String) = scenario("DSL Query5 " + env)
    .forever(){
      exec(
        http("DSL Query5 PUT")
        .put("/aai/v21/dsl?format=resource")
        .headers(httpHeaders)
        .body(ElFileBody("onap-DSL5.json"))
        .check(
          status.find.in(200)
        )
      )
      .pause(2,13)
    }

            /***
    *
    * @param env
    * @DSL6
    */
 def DslQuery6(env: String) = scenario("DSL Query6 " + env)
    .forever(){
      exec(
        http("DSL Query6 PUT")
        .put("/aai/v21/dsl?format=resource")
        .headers(httpHeaders)
        .body(ElFileBody("onap-DSL6.json"))
        .check(
          status.find.in(200)
        )
      )
      .pause(2,14)
    }

            /***
    *
    * @param env
    * @DSL7
    */
 def DslQuery7(env: String) = scenario("DSL Query7 " + env)
    .forever(){
      exec(
        http("DSL Query7 PUT")
        .put("/aai/v21/dsl?format=resource")
        .headers(httpHeaders)
        .body(ElFileBody("onap-DSL7.json"))
        .check(
          status.find.in(200)
        )
      )
      .pause(2,19)
    }

            /***
    *
    * @param env
    * @DSL8
    */
 def DslQuery8(env: String) = scenario("DSL Query8 " + env)
    .forever(){
      exec(
        http("DSL Query8 PUT")
        .put("/aai/v21/dsl?format=resource")
        .headers(httpHeaders)
        .body(ElFileBody("onap-DSL8.json"))
        .check(
          status.find.in(200)
        )
      )
      .pause(2,9)
    }

            /***
    *
    * @param env
    * @DSL9
    */
 def DslQuery9(env: String) = scenario("DSL Query9 " + env)
    .forever(){
      exec(
        http("DSL Query9 PUT")
        .put("/aai/v21/dsl?format=resource")
        .headers(httpHeaders)
        .body(ElFileBody("onap-DSL9.json"))
        .check(
          status.find.in(200)
        )
      )
      .pause(2,7)
    }

            /***
    *
    * @param env
    * @DSL10
    */
 def DslQuery10(env: String) = scenario("DSL Query10 " + env)
    .forever(){
      exec(
        http("DSL Query10 PUT")
        .put("/aai/v21/dsl?format=resource")
        .headers(httpHeaders)
        .headers(Map(
          "X-DslApiVersion"-> """V2"""
        ))
        .body(ElFileBody("onap-DSL10.json"))
        .check(
          status.find.in(200)
        )
      )
      .pause(2,32)
    }


  /***
    *
    * @param env
    * @Does a Search NodesQuery request
    */
  def SearchNodesQuery(env: String) = scenario("GET VNF Search Nodes Query " + env)
    .forever(){
       exec(
        http("GET VNF Search Nodes Query " + env)
        .get("/aai/v23/search/nodes-query?search-node-type=generic-vnf&filter=vnf-name:EQUALS:onap-genericvnfname")
        .headers(httpHeaders)
        .headers(Map(
          "X-FromAppId"-> """ONAP-fromappId"""
        ))
        .check(
          status.find.in(200)
        )
      )
    .pause(2,36)
    }


  setUp(
      PutTenant("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
PatchTenant("ePerf02").inject(atOnceUsers(23)).protocols(http.baseURL("Enter Host with Port")),
GetTenant("ePerf02").inject(atOnceUsers(7)).protocols(http.baseURL("Enter Host with Port")),
DeleteTenant("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
Getvserver1("ePerf02").inject(atOnceUsers(4)).protocols(http.baseURL("Enter Host with Port")),
GetVFModule1("ePerf02").inject(atOnceUsers(2)).protocols(http.baseURL("Enter Host with Port")),
Getvserver2("ePerf02").inject(atOnceUsers(2)).protocols(http.baseURL("Enter Host with Port")),
GetSI1("ePerf02").inject(atOnceUsers(2)).protocols(http.baseURL("Enter Host with Port")),
GetVNF1("ePerf02").inject(atOnceUsers(2)).protocols(http.baseURL("Enter Host with Port")),
GetVNF2("ePerf02").inject(atOnceUsers(2)).protocols(http.baseURL("Enter Host with Port")),
GetPserver1("ePerf02").inject(atOnceUsers(2)).protocols(http.baseURL("Enter Host with Port")),
Getvolumegroup("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
Getplatform("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
Getsriovvf1("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
Getpnf1("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
Getvnfc1("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
Getvlan("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
GetSI2("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
GetPserver2("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
Getconfiguration("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
Getservicesubscription("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
Getpnf2("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
Getcustomer("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
Getowningentity("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
Getvnfc2("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
Getvlantag("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
Getproject("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
Getlob("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
Getsriovvf2("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
Getsnapshot("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
Getvolume("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
GetCR("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
PUTvfmodule("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
Getallservices("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
PostBulkSingleTx1("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
PostBulkSingleTx2("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
PostBulkSingleTx3("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
CQpserverzdevice1("ePerf02").inject(atOnceUsers(4)).protocols(http.baseURL("Enter Host with Port")),
CQpserverzdevice2("ePerf02").inject(atOnceUsers(2)).protocols(http.baseURL("Enter Host with Port")),
CQvlanrangefromVlantag("ePerf02").inject(atOnceUsers(2)).protocols(http.baseURL("Enter Host with Port")),
CQgetClfiRoadmTailSummary("ePerf02").inject(atOnceUsers(2)).protocols(http.baseURL("Enter Host with Port")),
CQvrfsfromVlantag("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
CQgenericVnfsfromPserver("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
CQzPnfsfromPnf("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
DslQuery1("ePerf02").inject(atOnceUsers(2)).protocols(http.baseURL("Enter Host with Port")),
DslQuery2("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
DslQuery3("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
DslQuery4("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
DslQuery5("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
DslQuery6("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
DslQuery7("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
DslQuery8("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
DslQuery9("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
DslQuery10("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port")),
SearchNodesQuery("ePerf02").inject(atOnceUsers(1)).protocols(http.baseURL("Enter Host with Port"))

  ).maxDuration(10 minutes)
}

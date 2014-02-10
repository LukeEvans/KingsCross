package com.reactor.kingscross.bootstrap

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import com.reactor.kingscross.news.News
import com.reactor.kingscross.config.NewsConfig
import com.reactor.kingscross.config.Config
import com.reactor.base.patterns.pull.FlowControlFactory
import com.reactor.base.patterns.pull.FlowControlConfig
import com.reactor.base.patterns.pull.FlowControlArgs
import com.reactor.base.patterns.pull.FlowControlConfig
import com.reactor.kingscross.control.CollectorArgs
import com.reactor.kingscross.control.StorerArgs
import com.reactor.kingscross.news.sources._
import com.reactor.kingscross.control.CollectorArgs
import com.reactor.kingscross.control.StorerArgs
import com.reactor.base.patterns.pull.FlowControlConfig
import com.reactor.kingscross.control.CollectorArgs
import com.reactor.kingscross.control.StorerArgs
import com.reactor.base.patterns.pull.FlowControlConfig
import com.reactor.kingscross.control.CollectorArgs
import com.reactor.kingscross.control.StorerArgs
import com.reactor.base.patterns.pull.FlowControlConfig

class NewsBootstrap extends Actor with ActorLogging {
 
  // Init
  storers()
  collector()
  general()
  custom()
  
  //================================================================================
  // Storers
  //================================================================================
  def storers() {
	  val storersConfig = new Config(emitPlatform="/news", collectPlatform="/news", storePlatform="/news")
	  
	  // Mongo
	  val mongoFlowConfig = FlowControlConfig(name="newsMongoStorer", actorType="com.reactor.kingscross.news.NewsMongoStorer")
	  val mongoStorer = FlowControlFactory.flowControlledActorFor(context, mongoFlowConfig, StorerArgs(config=storersConfig, storeType="News"))
	  
	  val devStorersConfig = new Config(emitPlatform="/news/dev", collectPlatform="/news/dev", storePlatform="/news/dev")
	  val devMongoStorer = FlowControlFactory.flowControlledActorFor(context, mongoFlowConfig, StorerArgs(config=devStorersConfig, storeType="News-Dev"))
	  
	  // Elasticsearch
	  //val esFlowConfig = FlowControlConfig(name="newsMongoStorer", actorType="com.reactor.kingscross.news.NewsESStorer")
	  //val esStorer = FlowControlFactory.flowControlledActorFor(context, esFlowConfig, StorerArgs(config=storersConfig, storeType="News"))
	  
	  // Titan
	  //val titanFlowConfig = FlowControlConfig(name="newsMongoStorer", actorType="com.reactor.kingscross.news.NewsTitanStorer")
	  //val titanStorer = FlowControlFactory.flowControlledActorFor(context, titanFlowConfig, StorerArgs(config=storersConfig, storeType="News"))
  }
  
  //================================================================================
  // Standard news collector
  //================================================================================
  def collector() {
	  // Collector
	  val flowConfig = FlowControlConfig(name="newsCollector", actorType="com.reactor.kingscross.news.NewsCollector")
	  val collectorConfig = new Config(collectPlatform="/news", storePlatform="/news")
	  val collector = FlowControlFactory.flowControlledActorFor(context, flowConfig, CollectorArgs(config=collectorConfig))    
  }
  
  //================================================================================
  // General news 
  //================================================================================
  def general() {
//    val atlantic = context.actorOf(Props(classOf[AtlanticNews], new NewsConfig(id="atlantic",url="http://feeds.feedburner.com/TheAtlantic?format=xml",emitPlatform="/news/atlantic",collectPlatform="/news/atlantic",pollTime=5000)))
//    val wsj = context.actorOf(Props(classOf[WallStreetJournalNews], new NewsConfig(id="wsj",url="http://online.wsj.com/xml/rss/3_7014.xml",emitPlatform="/news/wsj",collectPlatform="/news/wsj",pollTime=5000)))
//    val cnnPolitics = context.actorOf(Props(classOf[CNNPoliticsNews], new NewsConfig(id="cnn_politics",url="http://rss.cnn.com/rss/cnn_allpolitics.rss",emitPlatform="/news/cnn_politics",collectPlatform="/news/cnn_politics",pollTime=5000)))
//    val yahooSportsNhl = context.actorOf(Props(classOf[YahooSportsNhlNews], new NewsConfig(id="yahoosports_nhl",url="http://sports.yahoo.com/nhl/rss.xml",emitPlatform="/news/yahoosports_nhl",collectPlatform = "/news/yahoosports_nhl",pollTime = 5000)))
//    val nhl = context.actorOf(Props(classOf[NhlNews], new NewsConfig(id="nhlfeatured",url="http://www.nhl.com/rss/features.xml",emitPlatform = "/news/nhlfeatured",collectPlatform = "/news/nhlfeatured",pollTime = 5000)))
//
//    val theHockeyNewsId:String = "thehockeynews"
//    val theHockeyNews = context.actorOf(Props(classOf[TheHockeyNewsNews], new NewsConfig(id=theHockeyNewsId,url="http://thehockeynews.com.feedsportal.com/c/34166/f/621201/index.rss",emitPlatform = "/news/"+theHockeyNewsId,collectPlatform = "/news/"+theHockeyNewsId,pollTime = 5000)))
//
//    val simplyRecipesID = "simplyRecipes"
//    val simplyRecipes = context.actorOf(Props(classOf[SimplyRecipesNews], new NewsConfig(id=simplyRecipesID,url="http://feeds.feedburner.com/elise/simplyrecipes",emitPlatform = "/news/"+simplyRecipesID,collectPlatform = "/news/"+simplyRecipesID,pollTime = 5000)))
//
//    val lottieDoofID = "lottieanddoof"
//    val lottieDoof = context.actorOf(Props(classOf[LottieDoofNews], new NewsConfig(id=lottieDoofID,url="http://feeds.feedburner.com/lottieanddoof",emitPlatform = "/news/"+lottieDoofID,collectPlatform = "/news/"+lottieDoofID,pollTime = 5000)))
//
//    val proFootballTalkID = "pro_football_talk"
//    val proFootballTalk = context.actorOf(Props(classOf[ProFootballTalkNews], new NewsConfig(id=proFootballTalkID,url="http://profootballtalk.nbcsports.com/category/rumor-mill/feed/atom/",emitPlatform = "/news/"+proFootballTalkID,collectPlatform = "/news/"+proFootballTalkID,pollTime = 5000)))
//
   // val adventureBlogID = "advblog"
    //val adventureBlog = context.actorOf(Props(classOf[AdventureBlogNews], new NewsConfig(id=adventureBlogID,url="http://feeds.feedburner.com/theadventureblog?format=xml",emitPlatform = "/news/"+adventureBlogID,collectPlatform = "/news/"+adventureBlogID,pollTime = 5000)))

    //val incID = "inc"
    //val inc =  context.actorOf(Props(classOf[IncNews], new NewsConfig(id=incID,url="http://www.inc.com/rss/homepage.xml",emitPlatform = "/news/"+incID,collectPlatform = "/news/"+incID,pollTime = 5000)))

    //val bbcScienceID = "bbc_science"
    //val bbcScience = context.actorOf(Props(classOf[BBCScienceNews], new NewsConfig(id=bbcScienceID,url="http://feeds.bbci.co.uk/news/science_and_environment/rss.xml",emitPlatform = "/news/"+bbcScienceID,collectPlatform = "/news/"+bbcScienceID,pollTime = 5000)))

    //val bbcEntertainmentID = "bbc_entertainment"
    //val bbcEntertainment = context.actorOf(Props(classOf[BBCEntertainmentNews], new NewsConfig(id=bbcEntertainmentID,url="http://feeds.bbci.co.uk/news/entertainment_and_arts/rss.xml",emitPlatform = "/news/"+bbcEntertainmentID,collectPlatform = "/news/"+bbcEntertainmentID,pollTime = 5000)))

    //val bbcBusinessID = "bbc_business"
    //val bbcBusiness = context.actorOf(Props(classOf[BBCBusinessNews], new NewsConfig(id=bbcBusinessID,url="http://feeds.bbci.co.uk/news/business/rss.xml",emitPlatform = "/news/"+bbcBusinessID,collectPlatform = "/news/"+bbcBusinessID,pollTime = 5000)))

    //val bbcPoliticsID = "bbc_politics"
    //val bbcPolitics = context.actorOf(Props(classOf[BBCPoliticsNews], new NewsConfig(id=bbcPoliticsID,url="http://feeds.bbci.co.uk/news/politics/rss.xml",emitPlatform = "/news/"+bbcPoliticsID,collectPlatform = "/news/"+bbcPoliticsID,pollTime = 5000)))

    //val bbcHealthID = "bbc_health"
    //val bbcHealth = context.actorOf(Props(classOf[BBCHealthNews], new NewsConfig(id=bbcHealthID,url="http://feeds.bbci.co.uk/news/health/rss.xml",emitPlatform = "/news/"+bbcHealthID,collectPlatform = "/news/"+bbcHealthID,pollTime = 5000)))

    //val bbcEducationID = "bbc_education"
    //val bbcEducation =  context.actorOf(Props(classOf[BBCEducationNews], new NewsConfig(id=bbcEducationID,url="http://feeds.bbci.co.uk/news/education/rss.xml",emitPlatform = "/news/"+bbcEducationID,collectPlatform = "/news/"+bbcEducationID,pollTime = 5000)))

    //val fashionologieID = "fashionologie"
    //val fashionologie = context.actorOf(Props(classOf[FashionologieNews], new NewsConfig(id=fashionologieID,url="http://www.fashionologie.com/posts/feed",emitPlatform = "/news/"+fashionologieID,collectPlatform = "/news/"+fashionologieID,pollTime = 5000)))

    val vergeID = "the_verge"
    val verge = context.actorOf(Props(classOf[VergeNews], new NewsConfig(id=vergeID,url="http://www.theverge.com/rss/index.xml",emitPlatform = "/news/"+vergeID,collectPlatform = "/news/"+vergeID,pollTime = 5000)))

    val techcrunchID = "techcrunch"
    val techcrunch = context.actorOf(Props(classOf[TechCrunchNews], new NewsConfig(id=techcrunchID,url="http://feeds.feedburner.com/TechCrunch/",emitPlatform = "/news/"+techcrunchID,collectPlatform = "/news/"+techcrunchID,pollTime = 5000)))
  }
  
  
  //================================================================================
  // Custom News 
  //================================================================================
  def custom() {
	//val techcrunch = context.actorOf(Props(classOf[Techcrunch], new NewsConfig(id="techcrunch", url="www.techcrunch.com", emitPlatform="/news/techcrunch", collectPlatform="/news/techcrunch", pollTime=15)))    
  }
  
  // Ignore messages
  def receive = { case _ => }
}

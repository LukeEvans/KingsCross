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

    val standardPollTime:Int = 5*60

    val atlantic = context.actorOf(Props(classOf[AtlanticNews], new NewsConfig(id="atlantic",url="http://feeds.feedburner.com/TheAtlantic?format=xml",emitPlatform="/news/atlantic",collectPlatform="/news/atlantic",pollTime=standardPollTime)))
    val wsj = context.actorOf(Props(classOf[WallStreetJournalNews], new NewsConfig(id="wsj",url="http://online.wsj.com/xml/rss/3_7014.xml",emitPlatform="/news/wsj",collectPlatform="/news/wsj",pollTime=standardPollTime)))
    val cnnPolitics = context.actorOf(Props(classOf[CNNPoliticsNews], new NewsConfig(id="cnn_politics",url="http://rss.cnn.com/rss/cnn_allpolitics.rss",emitPlatform="/news/cnn_politics",collectPlatform="/news/cnn_politics",pollTime=standardPollTime)))
    val yahooSportsNhl = context.actorOf(Props(classOf[YahooSportsNhlNews], new NewsConfig(id="yahoosports_nhl",url="http://sports.yahoo.com/nhl/rss.xml",emitPlatform="/news/yahoosports_nhl",collectPlatform = "/news/yahoosports_nhl",pollTime = standardPollTime)))
    val nhl = context.actorOf(Props(classOf[NhlNews], new NewsConfig(id="nhlfeatured",url="http://www.nhl.com/rss/features.xml",emitPlatform = "/news/nhlfeatured",collectPlatform = "/news/nhlfeatured",pollTime = standardPollTime)))

    val theHockeyNewsId:String = "thehockeynews"
    val theHockeyNews = context.actorOf(Props(classOf[TheHockeyNewsNews], new NewsConfig(id=theHockeyNewsId,url="http://thehockeynews.com.feedsportal.com/c/34166/f/621201/index.rss",emitPlatform = "/news/"+theHockeyNewsId,collectPlatform = "/news/"+theHockeyNewsId,pollTime = standardPollTime)))

    val simplyRecipesID = "simplyRecipes"
    val simplyRecipes = context.actorOf(Props(classOf[SimplyRecipesNews], new NewsConfig(id=simplyRecipesID,url="http://feeds.feedburner.com/elise/simplyrecipes",emitPlatform = "/news/"+simplyRecipesID,collectPlatform = "/news/"+simplyRecipesID,pollTime = standardPollTime)))

    val lottieDoofID = "lottieanddoof"
    val lottieDoof = context.actorOf(Props(classOf[LottieDoofNews], new NewsConfig(id=lottieDoofID,url="http://feeds.feedburner.com/lottieanddoof",emitPlatform = "/news/"+lottieDoofID,collectPlatform = "/news/"+lottieDoofID,pollTime = standardPollTime)))

    val proFootballTalkID = "pro_football_talk"
    val proFootballTalk = context.actorOf(Props(classOf[ProFootballTalkNews], new NewsConfig(id=proFootballTalkID,url="http://profootballtalk.nbcsports.com/category/rumor-mill/feed/atom/",emitPlatform = "/news/"+proFootballTalkID,collectPlatform = "/news/"+proFootballTalkID,pollTime = standardPollTime)))

    val adventureBlogID = "advblog"
    val adventureBlog = context.actorOf(Props(classOf[AdventureBlogNews], new NewsConfig(id=adventureBlogID,url="http://feeds.feedburner.com/theadventureblog?format=xml",emitPlatform = "/news/"+adventureBlogID,collectPlatform = "/news/"+adventureBlogID,pollTime = standardPollTime)))

    val incID = "inc"
    val inc =  context.actorOf(Props(classOf[IncNews], new NewsConfig(id=incID,url="http://www.inc.com/rss/homepage.xml",emitPlatform = "/news/"+incID,collectPlatform = "/news/"+incID,pollTime = standardPollTime)))

    val bbcScienceID = "bbc_science"
    val bbcScience = context.actorOf(Props(classOf[BBCScienceNews], new NewsConfig(id=bbcScienceID,url="http://feeds.bbci.co.uk/news/science_and_environment/rss.xml",emitPlatform = "/news/"+bbcScienceID,collectPlatform = "/news/"+bbcScienceID,pollTime = standardPollTime)))

    val bbcEntertainmentID = "bbc_entertainment"
    val bbcEntertainment = context.actorOf(Props(classOf[BBCEntertainmentNews], new NewsConfig(id=bbcEntertainmentID,url="http://feeds.bbci.co.uk/news/entertainment_and_arts/rss.xml",emitPlatform = "/news/"+bbcEntertainmentID,collectPlatform = "/news/"+bbcEntertainmentID,pollTime = standardPollTime)))

    val bbcBusinessID = "bbc_business"
    val bbcBusiness = context.actorOf(Props(classOf[BBCBusinessNews], new NewsConfig(id=bbcBusinessID,url="http://feeds.bbci.co.uk/news/business/rss.xml",emitPlatform = "/news/"+bbcBusinessID,collectPlatform = "/news/"+bbcBusinessID,pollTime = standardPollTime)))

    val bbcPoliticsID = "bbc_politics"
    val bbcPolitics = context.actorOf(Props(classOf[BBCPoliticsNews], new NewsConfig(id=bbcPoliticsID,url="http://feeds.bbci.co.uk/news/politics/rss.xml",emitPlatform = "/news/"+bbcPoliticsID,collectPlatform = "/news/"+bbcPoliticsID,pollTime = standardPollTime)))

    val bbcHealthID = "bbc_health"
    val bbcHealth = context.actorOf(Props(classOf[BBCHealthNews], new NewsConfig(id=bbcHealthID,url="http://feeds.bbci.co.uk/news/health/rss.xml",emitPlatform = "/news/"+bbcHealthID,collectPlatform = "/news/"+bbcHealthID,pollTime = standardPollTime)))

    val bbcEducationID = "bbc_education"
    val bbcEducation =  context.actorOf(Props(classOf[BBCEducationNews], new NewsConfig(id=bbcEducationID,url="http://feeds.bbci.co.uk/news/education/rss.xml",emitPlatform = "/news/"+bbcEducationID,collectPlatform = "/news/"+bbcEducationID,pollTime = standardPollTime)))

    val fashionologieID = "fashionologie"
    val fashionologie = context.actorOf(Props(classOf[FashionologieNews], new NewsConfig(id=fashionologieID,url="http://www.fashionologie.com/posts/feed",emitPlatform = "/news/"+fashionologieID,collectPlatform = "/news/"+fashionologieID,pollTime = standardPollTime)))

    val vergeID = "the_verge"
    val verge = context.actorOf(Props(classOf[VergeNews], new NewsConfig(id=vergeID,url="http://www.theverge.com/rss/index.xml",emitPlatform = "/news/"+vergeID,collectPlatform = "/news/"+vergeID,pollTime = standardPollTime)))

    val techcrunchID = "techcrunch"
    val techcrunch = context.actorOf(Props(classOf[TechCrunchNews], new NewsConfig(id=techcrunchID,url="http://feeds.feedburner.com/TechCrunch/",emitPlatform = "/news/"+techcrunchID,collectPlatform = "/news/"+techcrunchID,pollTime = standardPollTime)))

    val usaTodaySportsID = "usatoday_sports"
    val usaTodaySports =   context.actorOf(Props(classOf[UsaTodaySportsNews], new NewsConfig(id=usaTodaySportsID,url="http://rssfeeds.usatoday.com/UsatodaycomSports-TopStories",emitPlatform = "/news/"+usaTodaySportsID,collectPlatform = "/news/"+usaTodaySportsID,pollTime = standardPollTime)))

    val usaTodayHockeyID = "usatoday_hockey"
    val usaTodayHockey = context.actorOf(Props(classOf[UsaTodayHockeyNews], new NewsConfig(id=usaTodayHockeyID,url="http://rssfeeds.usatoday.com/UsatodaycomNhl-TopStories",emitPlatform = "/news/"+usaTodayHockeyID,collectPlatform = "/news/"+usaTodayHockeyID,pollTime = standardPollTime)))

    val onionID = "the_onion"
    val onion = context.actorOf(Props(classOf[OnionNews], new NewsConfig(id=onionID,url="http://feeds.theonion.com/theonion/daily",emitPlatform = "/news/"+onionID,collectPlatform = "/news/"+onionID,pollTime = standardPollTime)))

    val engadgetID = "engadget"
    val engadget = context.actorOf(Props(classOf[EngadgetNews], new NewsConfig(id=engadgetID,url="http://www.engadget.com/rss.xml",emitPlatform = "/news/"+engadgetID,collectPlatform = "/news/"+engadgetID,pollTime = standardPollTime)))

    val guardianID = "guardian"
    val guardian = context.actorOf(Props(classOf[GuardianNews], new NewsConfig(id=guardianID,url="http://feeds.guardian.co.uk/theguardian/us-home/rss",emitPlatform = "/news/"+guardianID,collectPlatform = "/news/"+guardianID,pollTime = standardPollTime)))

    val washPostID = "washpost_world"
    val washPost = context.actorOf(Props(classOf[WashingtonPostNews], new NewsConfig(id=washPostID,url="http://feeds.washingtonpost.com/rss/world",emitPlatform = "/news/"+washPostID,collectPlatform = "/news/"+washPostID,pollTime = standardPollTime)))

    val whiteOnRiceID = "whiteonrice"
    val whiteOnRice = context.actorOf(Props(classOf[WhiteOnRiceNews], new NewsConfig(id=whiteOnRiceID,url="http://feeds.feedburner.com/worc",emitPlatform = "/news/"+whiteOnRiceID,collectPlatform = "/news/"+whiteOnRiceID,pollTime = standardPollTime)))

    val reutersPoliticsID = "reuters_politics"
    val reutersPolitics = context.actorOf(Props(classOf[ReutersPoliticsNews], new NewsConfig(id=reutersPoliticsID,url="http://feeds.reuters.com/Reuters/PoliticsNews?format=xml",emitPlatform = "/news/"+reutersPoliticsID,collectPlatform = "/news/"+reutersPoliticsID,pollTime = standardPollTime)))

    val reutersWorldID = "reuters_world"
    val reutersWorld = context.actorOf(Props(classOf[ReutersWorldNews], new NewsConfig(id=reutersWorldID,url="http://feeds.reuters.com/Reuters/worldNews?format=xml",emitPlatform = "/news/"+reutersWorldID,collectPlatform = "/news/"+reutersWorldID,pollTime = standardPollTime)))

    val reutersEntertainmentID = "reuters_entertainment"
    val reutersEntertainment = context.actorOf(Props(classOf[ReutersEntertainmentNews], new NewsConfig(id=reutersEntertainmentID,url="http://feeds.reuters.com/reuters/entertainment?format=xml",emitPlatform = "/news/"+reutersEntertainmentID,collectPlatform = "/news/"+reutersEntertainmentID,pollTime = standardPollTime)))

    val reutersTopID = "reuters_top"
    val reutersTop = context.actorOf(Props(classOf[ReutersTopNews], new NewsConfig(id=reutersTopID,url="http://feeds.reuters.com/reuters/topNews?format=xml",emitPlatform = "/news/"+reutersTopID,collectPlatform = "/news/"+reutersTopID,pollTime = standardPollTime)))

    val reutersBusinessID = "reuters_business"
    val reutersBusiness = context.actorOf(Props(classOf[ReutersBusinessNews], new NewsConfig(id=reutersBusinessID,url="http://feeds.reuters.com/reuters/businessNews?format=xml",emitPlatform = "/news/"+reutersBusinessID,collectPlatform = "/news/"+reutersBusinessID,pollTime = standardPollTime)))

    val reutersLifestyleID = "reuters_lifestyle"
    val reutersLifestyle = context.actorOf(Props(classOf[ReutersLifestyleNews], new NewsConfig(id=reutersLifestyleID,url="http://feeds.reuters.com/reuters/lifestyle?format=xml",emitPlatform = "/news/"+reutersLifestyleID,collectPlatform = "/news/"+reutersLifestyleID,pollTime = standardPollTime)))

    val joystiqID = "reuters_joystiq"
    val joystiq = context.actorOf(Props(classOf[JoystiqNews], new NewsConfig(id=joystiqID,url="http://www.joystiq.com/rss.xml",emitPlatform = "/news/"+joystiqID,collectPlatform = "/news/"+joystiqID,pollTime = standardPollTime)))

    val nprBusinessID = "bus"
    val nprBusiness = context.actorOf(Props(classOf[NPRBusinessNews], new NewsConfig(id=nprBusinessID,url="http://www.npr.org/rss/rss.php?id=1006",emitPlatform = "/news/"+nprBusinessID,collectPlatform = "/news/"+nprBusinessID,pollTime = standardPollTime)))

    val nprHeadlineID = "top"
    val nprHeadline = context.actorOf(Props(classOf[NPRHeadlineNews], new NewsConfig(id=nprHeadlineID,url="http://www.npr.org/rss/rss.php?id=1001",emitPlatform = "/news/"+nprHeadlineID,collectPlatform = "/news/"+nprHeadlineID,pollTime = standardPollTime)))

    val nprPoliticsID = "pol"
    val nprPolitics = context.actorOf(Props(classOf[NPRPoliticsNews], new NewsConfig(id=nprPoliticsID,url="http://www.npr.org/rss/rss.php?id=1012",emitPlatform = "/news/"+nprPoliticsID,collectPlatform = "/news/"+nprPoliticsID,pollTime = standardPollTime)))

    val nprSportsID = "spr"
    val nprSports = context.actorOf(Props(classOf[NPRSportsNews], new NewsConfig(id=nprSportsID,url="http://www.npr.org/rss/rss.php?id=1055",emitPlatform = "/news/"+nprSportsID,collectPlatform = "/news/"+nprSportsID,pollTime = standardPollTime)))

    val nprTechID = "tec"
    val nprTech = context.actorOf(Props(classOf[NPRTechnologyNews], new NewsConfig(id=nprTechID,url="http://www.npr.org/rss/rss.php?id=1019",emitPlatform = "/news/"+nprTechID,collectPlatform = "/news/"+nprTechID,pollTime = standardPollTime)))

    val nprWorldID = "wld"
    val nprWorld = context.actorOf(Props(classOf[NPRWorldNews], new NewsConfig(id=nprWorldID,url="http://www.npr.org/rss/rss.php?id=1004",emitPlatform = "/news/"+nprWorldID,collectPlatform = "/news/"+nprWorldID,pollTime = standardPollTime)))

    val nprEntertainmentID = "ent"
    val nprEntertainment = context.actorOf(Props(classOf[NPREntertainmentNews], new NewsConfig(id=nprEntertainmentID,url="http://www.npr.org/rss/rss.php?id=1048",emitPlatform = "/news/"+nprEntertainmentID,collectPlatform = "/news/"+nprEntertainmentID,pollTime = standardPollTime)))

    val foxWorldID = "fox_news_world"
    val foxWorld = context.actorOf(Props(classOf[FoxWorldNews], new NewsConfig(id=foxWorldID,url="http://feeds.foxnews.com/foxnews/world?format=xml",emitPlatform = "/news/"+foxWorldID,collectPlatform = "/news/"+foxWorldID,pollTime = standardPollTime)))

    val foxPoliticsID = "fox_news_politics"
    val foxPolitics = context.actorOf(Props(classOf[FoxPoliticsNews], new NewsConfig(id=foxPoliticsID,url="http://feeds.foxnews.com/foxnews/politics?format=xml",emitPlatform = "/news/"+foxPoliticsID,collectPlatform = "/news/"+foxPoliticsID,pollTime = standardPollTime)))

    val natGeoID = "national_geographic_news"
    val natGeo = context.actorOf(Props(classOf[NationalGeographicNews], new NewsConfig(id=natGeoID,url="http://feeds.nationalgeographic.com/ng/News/News_Main?format=xml",emitPlatform = "/news/"+natGeoID,collectPlatform = "/news/"+natGeoID,pollTime = standardPollTime)))

    val espnID = "espn"
    val espn = context.actorOf(Props(classOf[ESPNNews], new NewsConfig(id=espnID,url="http://api.espn.com/v1/sports/news/headlines/top?apikey=mtwby5psjv48mh7eemys3yqe",emitPlatform = "/news/"+espnID,collectPlatform = "/news/"+espnID,pollTime = standardPollTime)))

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

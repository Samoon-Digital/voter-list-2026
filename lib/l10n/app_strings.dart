import 'package:shared_preferences/shared_preferences.dart';

class AppStrings {
  static String _languageCode = 'en';

  static void setCachedLanguage(String? languageCode) {
    _languageCode = languageCode ?? 'en';
  }

  static Future<void> init() async {
    final prefs = await SharedPreferences.getInstance();
    _languageCode = prefs.getString('language_code') ?? 'en';
  }

  static Future<void> setLanguage(String languageCode) async {
    _languageCode = languageCode;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('language_code', languageCode);
  }

  static String get currentLanguage => _languageCode;

  static String tr(String key) {
    if (_localizedValues.containsKey(key)) {
      return _localizedValues[key]?[_languageCode] ?? key;
    }
    return key;
  }

  // Define Keys
  static const String appTitle = 'appTitle';
  static const String homeHeaderTitle = 'homeHeaderTitle';
  static const String homeHeaderDesc = 'homeHeaderDesc';
  static const String unofficialTag = 'unofficialTag';

  static const String draftListTitle = 'draftListTitle';
  static const String draftListDesc = 'draftListDesc';
  static const String downloadNow = 'downloadNow';

  static const String deletedListTitle = 'deletedListTitle';
  static const String deletedListDesc = 'deletedListDesc';
  static const String startSearch = 'startSearch';

  static const String oldListTitle = 'oldListTitle';
  static const String oldListDesc = 'oldListDesc';
  static const String download = 'download';

  static const String shareAppTitle = 'shareAppTitle';
  static const String shareAppDesc = 'shareAppDesc';
  static const String shareMessage = 'shareMessage';

  static const String rateUsTitle = 'rateUsTitle';
  static const String rateUsDesc = 'rateUsDesc';
  static const String rateUsFooter = 'rateUsFooter';

  static const String disclaimerTitle = 'disclaimerTitle';
  static const String disclaimerBody1 = 'disclaimerBody1';
  static const String disclaimerBody2 = 'disclaimerBody2';
  static const String readFullDisclaimer = 'readFullDisclaimer';
  static const String officialLinksTitle = 'officialLinksTitle';

  static const String madeWith = 'madeWith';
  static const String by = 'by';
  static const String contact = 'contact';

  static const String sendMessage = 'sendMessage';
  static const String sendMessageDesc = 'sendMessageDesc';
  static const String privacyPolicy = 'privacyPolicy';
  static const String termsConditions = 'termsConditions';
  static const String aboutUs = 'aboutUs';

  // Screen Titles
  static const String stateListTitle2003 = 'stateListTitle2003';
  static const String stateListTitle2026 = 'stateListTitle2026';
  static const String deletedSearchTitle = 'deletedSearchTitle';

  static const String searchDeletedHeader = 'searchDeletedHeader';

  static const String infoPopupDesc = 'infoPopupDesc';

  // Tamil Nadu Popup Options
  static const String download2002Pdf = 'download2002Pdf';
  static const String download2005Pdf = 'download2005Pdf';
  static const String otherOption = 'otherOption';

  // Jharkhand Popup Options
  static const String jhDownload2003 = 'jhDownload2003';
  static const String jhSearchName = 'jhSearchName';
  static const String jhHowToSearch = 'jhHowToSearch';

  // States
  static const String uttarPradesh = 'uttarPradesh';
  static const String uttarakhand = 'uttarakhand';
  static const String punjab = 'punjab';
  static const String goa = 'goa';
  static const String westBengal = 'westBengal';
  static const String madhyaPradesh = 'madhyaPradesh';
  static const String lakshadweep = 'lakshadweep';
  static const String chhattisgarh = 'chhattisgarh';
  static const String kerala = 'kerala';
  static const String andamanNicobar = 'andamanNicobar';
  static const String bihar = 'bihar';
  static const String rajasthan = 'rajasthan';
  static const String jammuKashmir = 'jammuKashmir';
  static const String tamilNadu = 'tamilNadu';
  static const String jharkhand = 'jharkhand';

  static const String chandigarh = 'chandigarh';
  static const String dadraNagar = 'dadraNagar';
  static const String delhi = 'delhi';
  static const String ladakh = 'ladakh';
  static const String puducherry = 'puducherry';
  static const String arunachalPradesh = 'arunachalPradesh';
  static const String apPollingStationWise = 'apPollingStationWise';
  static const String apAcWise = 'apAcWise';

  // Karnataka related strings
  static const String karnataka = 'karnataka';
  static const String kaDownload2002 = 'kaDownload2002';
  static const String kaSearchName = 'kaSearchName';

  static const String maharashtra = 'maharashtra';

  // Manipur related strings
  static const String manipur = 'manipur';
  static const String mnDownload2005 = 'mnDownload2005';
  static const String mnSearchName = 'mnSearchName';

  static const String meghalaya = 'meghalaya';

  // Nagaland related strings
  static const String nagaland = 'nagaland';
  static const String nlDownload2005 = 'nlDownload2005';
  static const String nlSearchName = 'nlSearchName';

  // Odisha related strings
  static const String odisha = 'odisha';
  static const String odDownload2005 = 'odDownload2005';
  static const String odSearchName = 'odSearchName';

  // Sikkim related strings
  static const String sikkim = 'sikkim';
  static const String skDownload2002 = 'skDownload2002';
  static const String skSearchName = 'skSearchName';

  // Telangana related strings
  static const String telangana = 'telangana';
  static const String tgDownload2002 = 'tgDownload2002';
  static const String tgSearchName = 'tgSearchName';

  // Tripura related strings
  static const String tripura = 'tripura';
  static const String trDownload2005 = 'trDownload2005';
  static const String trSearchName = 'trSearchName';

  // Andaman & Nicobar related strings
  static const String anDownload2002 = 'anDownload2002';
  static const String anSearchName = 'anSearchName';

  // Chandigarh related strings
  // chandigarh key is already defined in general states section
  static const String chDownload2002 = 'chDownload2002';
  static const String chSearchName = 'chSearchName';

  // Dadra & Nagar Haveli related strings
  // dadraNagar key is already defined in general states section
  static const String dnDownload2002 = 'dnDownload2002';
  static const String dnSearchName = 'dnSearchName';

  // Delhi related strings
  // delhi key is already defined in general states section
  static const String dlDownload2002 = 'dlDownload2002';
  static const String dlSearchName = 'dlSearchName';
  static const String dlPollingStation = 'dlPollingStation';
  static const String dlEpicSearch = 'dlEpicSearch';

  static const String changeLanguage = 'changeLanguage';

  static final Map<String, Map<String, String>> _localizedValues = {
    jharkhand: {'en': 'Jharkhand', 'hi': 'झारखंड'},
    changeLanguage: {'en': 'Change\nLanguage', 'hi': 'भाषा बदलें'},
    appTitle: {'en': 'Voterlist', 'hi': 'वोटर लिस्ट'},
    homeHeaderTitle: {
      'en': 'Download Electoral Rolls',
      'hi': 'मतदाता सूची डाउनलोड करें',
    },
    homeHeaderDesc: {
      'en':
          'Access the complete archive including Old 2002-2003 SIR Lists, the latest Draft Roll 2026-25-24, and Final Voter Lists. Download and verify your details easily.',
      'hi':
          'पुरानी 2002-2003 एसआईआर सूचियों, नवीनतम ड्राफ्ट रोल 2026-25-24 और अंतिम मतदाता सूचियों सहित पूर्ण संग्रह तक पहुंचें। अपना विवरण आसानी से डाउनलोड और सत्यापित करें।',
    },
    unofficialTag: {'en': '(Unofficial)', 'hi': '(अनौपचारिक)'},
    draftListTitle: {
      'en': 'Draft & Final List 2026',
      'hi': 'ड्राफ्ट और फाइनल लिस्ट 2026',
    },
    draftListDesc: {
      'en': 'Download latest Draft & Final Voter Lists 2026',
      'hi': 'नवीनतम ड्राफ्ट और फाइनल वोटर लिस्ट 2026 डाउनलोड करें',
    },
    downloadNow: {'en': 'Download Now', 'hi': 'अभी डाउनलोड करें'},
    deletedListTitle: {
      'en': 'Deleted Names List',
      'hi': 'हटाए गए नामों की सूची',
    },
    deletedListDesc: {
      'en': 'search your deleted name cause in draft list 2026',
      'hi': 'ड्राफ्ट लिस्ट 2026 में अपना नाम हटने का कारण देखें',
    },
    startSearch: {'en': 'Start Search', 'hi': 'खोजना शुरू करें'},
    oldListTitle: {'en': 'Old SIR Voter List', 'hi': 'पुरानी SIR वोटर लिस्ट'},
    oldListDesc: {
      'en': 'Download 2003 voter lists',
      'hi': '2003 वोटर लिस्ट डाउनलोड करें',
    },
    download: {'en': 'Download', 'hi': 'डाउनलोड'},
    shareAppTitle: {'en': 'Share this App', 'hi': 'ऐप शेयर करें'},
    shareAppDesc: {
      'en':
          'Help friends & family easily check their voter details & download list.',
      'hi':
          'दोस्तों और परिवार की मदद करें ताकि वे आसानी से अपना वोटर विवरण चेक और डाउनलोड कर सकें।',
    },
    shareMessage: {
      'en':
          'Download modern and old Voter Lists (2003-2026), Draft Rolls, and more easily with the Voterlist App. Get it here: ',
      'hi':
          'वोटर लिस्ट ऐप के माध्यम से पुरानी (2003-2002) और नई (2026) वोटर लिस्ट आसानी से डाउनलोड करें। अभी प्राप्त करें: ',
    },
    rateUsTitle: {'en': 'Rate Us 5 Stars', 'hi': 'हमें 5 स्टार दें'},
    rateUsFooter: {
      'en': 'Loved the app? Tap here to rate us 5 stars on Play Store!',
      'hi':
          'ऐप पसंद आया? प्ले स्टोर पर हमें 5 स्टार देने के लिए यहाँ टैप करें!',
    },
    disclaimerTitle: {'en': 'DISCLAIMER', 'hi': 'अस्वीकरण (DISCLAIMER)'},
    disclaimerBody1: {
      'en':
          'This is NOT an official government app. It is NOT linked with the Election Commission of India or any government agency, department, or individual.',
      'hi':
          'यह कोई आधिकारिक सरकारी ऐप नहीं है। यह भारत के चुनाव आयोग या किसी भी सरकारी एजेंसी, विभाग या व्यक्ति से जुड़ा नहीं है।',
    },
    disclaimerBody2: {
      'en':
          'This app is made ONLY for voter convenience for downloading and managing ERoll PDF files and searching electoral data.',
      'hi':
          'यह ऐप केवल मतदाताओं की सुविधा के लिए बनाया गया है ताकि वे ई-रोल पीडीएफ फाइलें डाउनलोड और मैनेज कर सकें और चुनावी डेटा खोज सकें।',
    },
    readFullDisclaimer: {
      'en': 'Read the full disclaimer',
      'hi': 'पूरा अस्वीकरण पढ़ें',
    },
    officialLinksTitle: {
      'en': 'OFFICIAL LINKS TO DOWNLOAD &\nSEARCH ELECTORAL ROLL',
      'hi': 'मतदाता सूची डाउनलोड और खोजने के लिए\nआधिकारिक लिंक',
    },
    madeWith: {'en': 'Made with ', 'hi': 'बनाया गया '},
    by: {'en': ' by ', 'hi': ' द्वारा '},
    contact: {'en': 'Contact: ', 'hi': 'संपर्क: '},
    sendMessage: {'en': 'Send Message', 'hi': 'संदेश भेजें'},
    sendMessageDesc: {
      'en': 'App related queries or feedback',
      'hi': 'ऐप से संबंधित प्रश्न या सुझाव',
    },
    privacyPolicy: {'en': 'Privacy Policy', 'hi': 'गोपनीयता नीति'},
    termsConditions: {'en': 'Terms & Conditions', 'hi': 'नियम एवं शर्तें'},
    aboutUs: {'en': 'About Us', 'hi': 'हमारे बारे में'},
    stateListTitle2003: {
      'en': 'Download Voter List (2002-2003)',
      'hi': 'वोटर लिस्ट डाउनलोड करें (2002-2003)',
    },
    stateListTitle2026: {
      'en': 'Download Draft List (2026)',
      'hi': 'ड्राफ्ट लिस्ट डाउनलोड करें (2026)',
    },
    deletedSearchTitle: {
      'en': 'Deleted Name Search 2026',
      'hi': 'हटाए गए नाम खोजें 2026',
    },
    searchDeletedHeader: {
      'en': 'Search Deleted names ASD List 2026',
      'hi': 'हटाए गए नाम / ASD लिस्ट 2026 खोजें',
    },

    // Info Popup Strings
    'infoPopupDesc': {
      'en':
          'Due to high active users site might be slow. This is an ad supported app.',
      'hi':
          'अधिक सक्रिय उपयोगकर्ताओं के कारण साइट धीमी हो सकती है। यह एक विज्ञापन समर्थित ऐप है।',
    },

    // Tamil Nadu Popup Options
    'download2002Pdf': {
      'en': 'Download 2002 PDF',
      'hi': '2002 पीडीएफ डाउनलोड करें',
    },
    'download2005Pdf': {
      'en': 'Download 2005 PDF',
      'hi': '2005 पीडीएफ डाउनलोड करें',
    },
    'otherOption': {'en': 'Other Option', 'hi': 'अन्य विकल्प'},

    // Jharkhand Popup Options
    'jhDownload2003': {
      'en': 'Download 2003 PDF',
      'hi': '2003 पीडीएफ डाउनलोड करें',
    },
    'jhSearchName': {'en': 'Search by Name', 'hi': 'नाम से खोजें'},
    'jhHowToSearch': {'en': 'How to Search Name', 'hi': 'नाम कैसे खोजें'},
    // States
    uttarPradesh: {'en': 'Uttar Pradesh', 'hi': 'उत्तर प्रदेश'},
    uttarakhand: {'en': 'Uttarakhand', 'hi': 'उत्तराखंड'},
    punjab: {'en': 'Punjab', 'hi': 'पंजाब'},
    goa: {'en': 'Goa', 'hi': 'गोवा'},
    westBengal: {'en': 'West Bengal', 'hi': 'पश्चिम बंगाल'},
    madhyaPradesh: {'en': 'Madhya Pradesh', 'hi': 'मध्य प्रदेश'},
    lakshadweep: {'en': 'Lakshadweep', 'hi': 'लक्षद्वीप'},
    chhattisgarh: {'en': 'Chattisgarh', 'hi': 'छत्तीसगढ़'},
    kerala: {'en': 'Kerala', 'hi': 'केरल'},
    andamanNicobar: {'en': 'Andaman & Nicobar', 'hi': 'अंडमान और निकोबार'},
    bihar: {'en': 'Bihar', 'hi': 'बिहार'},
    rajasthan: {'en': 'Rajasthan', 'hi': 'राजस्थान'},
    jammuKashmir: {'en': 'Jammu & Kashmir', 'hi': 'जम्मू और कश्मीर'},
    tamilNadu: {'en': 'Tamil Nadu', 'hi': 'तमिलनाडु'},
    chandigarh: {'en': 'Chandigarh', 'hi': 'चंडीगढ़'},
    dadraNagar: {
      'en': 'Dadra and Nagar Haveli & Daman and Diu',
      'hi': 'दादरा और नगर हवेली एवं दमन और दीव',
    },
    delhi: {'en': 'Delhi', 'hi': 'दिल्ली'},
    ladakh: {'en': 'Ladakh', 'hi': 'लद्दाख'},
    puducherry: {'en': 'Puducherry', 'hi': 'पुडुचेरी'},
    arunachalPradesh: {'en': 'Arunachal Pradesh', 'hi': 'अरुणाचल प्रदेश'},
    apPollingStationWise: {
      'en': '2006 List Polling Station Wise',
      'hi': '2006 सूची पोलिंग स्टेशन वार',
    },
    apAcWise: {'en': '2006 List AC Wise', 'hi': '2006 सूची एसी वार'},
    karnataka: {'en': 'Karnataka', 'hi': 'कर्नाटक'},
    kaDownload2002: {'en': '2002 List Download', 'hi': '2002 सूची डाउनलोड'},
    kaSearchName: {'en': '2002 List Search Name', 'hi': '2002 सूची नाम खोजें'},
    maharashtra: {'en': 'Maharashtra', 'hi': 'महाराष्ट्र'},
    manipur: {'en': 'Manipur', 'hi': 'मणिपुर'},
    mnDownload2005: {'en': '2005 List Download', 'hi': '2005 सूची डाउनलोड'},
    mnSearchName: {'en': '2002 List Search Name', 'hi': '2002 सूची नाम खोजें'},
    meghalaya: {'en': 'Meghalaya', 'hi': 'मेघालय'},
    nagaland: {'en': 'Nagaland', 'hi': 'नागालैंड'},
    nlDownload2005: {'en': '2005 List Download', 'hi': '2005 सूची डाउनलोड'},
    nlSearchName: {'en': '2002 List Search Name', 'hi': '2002 सूची नाम खोजें'},
    odisha: {'en': 'Odisha', 'hi': 'ओडिशा'},
    odDownload2005: {'en': '2005 List Download', 'hi': '2005 सूची डाउनलोड'},
    odSearchName: {'en': '2002 List Search Name', 'hi': '2002 सूची नाम खोजें'},
    sikkim: {'en': 'Sikkim', 'hi': 'सिक्किम'},
    skDownload2002: {'en': '2002 List Download', 'hi': '2002 सूची डाउनलोड'},
    skSearchName: {'en': '2002 List Search Name', 'hi': '2002 सूची नाम खोजें'},
    telangana: {'en': 'Telangana', 'hi': 'तेलंगाना'},
    tgDownload2002: {'en': '2002 List Download', 'hi': '2002 सूची डाउनलोड'},
    tgSearchName: {'en': '2002 List Search Name', 'hi': '2002 सूची नाम खोजें'},
    tripura: {'en': 'Tripura', 'hi': 'त्रिपुरा'},
    trDownload2005: {'en': '2005 List Download', 'hi': '2005 सूची डाउनलोड'},
    trSearchName: {'en': '2002 List Search Name', 'hi': '2002 सूची नाम खोजें'},
    anDownload2002: {'en': '2002 List Download', 'hi': '2002 सूची डाउनलोड'},
    anSearchName: {'en': '2002 List Search Name', 'hi': '2002 सूची नाम खोजें'},
    chDownload2002: {'en': '2002 List Download', 'hi': '2002 सूची डाउनलोड'},
    chSearchName: {'en': '2002 List Search Name', 'hi': '2002 सूची नाम खोजें'},
    dnDownload2002: {'en': '2002 List Download', 'hi': '2002 सूची डाउनलोड'},
    dnSearchName: {'en': '2002 List Search Name', 'hi': '2002 सूची नाम खोजें'},
    dlDownload2002: {'en': '2002 List Download', 'hi': '2002 सूची डाउनलोड'},
    dlSearchName: {'en': '2002 List Name Search', 'hi': '2002 सूची नाम खोजें'},
    dlPollingStation: {
      'en': '2002 List Polling Station Wise',
      'hi': '2002 सूची पोलिंग स्टेशन वार',
    },
    dlEpicSearch: {
      'en': '2002 List EPIC Number Wise',
      'hi': '2002 सूची इपिक नंबर वार',
    },
  };
}

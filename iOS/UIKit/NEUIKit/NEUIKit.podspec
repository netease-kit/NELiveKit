Pod::Spec.new do |s|
  s.name = 'NEUIKit'
  s.version = '1.0.0'
  s.summary = 'A short description of NEUIKit.'
  s.homepage = 'http://netease.im'
  s.license = {"type"=> "Copyright", "text"=> " Copyright 2022 Netease "}
  s.author = 'yunxin engineering department'
  s.platforms = {"ios"=> "12.0"}
  s.source = {"git"=> "https://github.com/netease-kit/"}
  s.source_files = 'NEUIKit/Classes/**/*'
  s.pod_target_xcconfig = {"EXCLUDED_ARCHS[sdk=iphonesimulator*]"=> "arm64", "BUILD_LIBRARY_FOR_DISTRIBUTION"=> "YES", "APPLICATION_EXTENSION_API_ONLY"=> "NO", "DEBUG_INFORMATION_FORMAT"=> "dwarf-with-dsym"}
  s.dependency 'Masonry'
end

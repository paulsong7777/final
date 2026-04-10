package com.moeats.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.moeats.domain.StoreMenu;
import com.moeats.mappers.OrderStoreMenuMapper;

@Service
public class MenuService {
	@Autowired
	OrderStoreMenuMapper storeMenuMapper;
	
	public StoreMenu findByIdx(int menuIdx) {
		return storeMenuMapper.findByIdx(menuIdx);
	}
	
	
	public List<StoreMenu> findByIdxs(Collection<Integer> menuIdxs) {
		List<StoreMenu> storeMenus;
		if(menuIdxs.isEmpty())
			storeMenus = List.of();
		else if(menuIdxs.size()<10000)
			storeMenus = storeMenuMapper.findByIdxs(new HashSet<Integer>(menuIdxs));
		else {
			storeMenus = new ArrayList<StoreMenu>();
			List<Integer> menuIdxList = new ArrayList<Integer>(menuIdxs);
			for(int i=0;i<menuIdxList.size();i+=10000)
				storeMenus.addAll(storeMenuMapper.findByIdxs(new HashSet<Integer>(menuIdxList.subList(i, Math.min(i+10000,menuIdxList.size())))));
		}
		return storeMenus;
	}
	
	public List<StoreMenu> findByIdxs2(Collection<Integer> menuIdxs) {
		List<StoreMenu> storeMenus;
		if(menuIdxs.isEmpty())
			storeMenus = List.of();
		else if(menuIdxs.size()<10000)
			storeMenus = storeMenuMapper.findByIdxs2(new ArrayList<Integer>(menuIdxs));
		else {
			storeMenus = new ArrayList<StoreMenu>();
			List<Integer> menuIdxList = new ArrayList<Integer>(menuIdxs);
			for(int i=0;i<menuIdxList.size();i+=10000)
				storeMenus.addAll(storeMenuMapper.findByIdxs2(new ArrayList<Integer>(menuIdxList.subList(i, Math.min(i+10000,menuIdxList.size())))));
		}
		return storeMenus;
	}
	
}

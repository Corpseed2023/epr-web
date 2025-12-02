package com.epr.service;

import com.epr.dto.blog.BlogRequestDto;
import com.epr.dto.blog.BlogResponseDto;

import java.util.List;

public interface BlogService {

    List<BlogResponseDto> findAllActiveBlogs();

    List<BlogResponseDto> searchBlogs(String keyword);

    BlogResponseDto findById(Long id);

    BlogResponseDto createBlog(BlogRequestDto dto, Long userId);

    BlogResponseDto updateBlog(Long id, BlogRequestDto dto, Long userId);

    void softDeleteBlog(Long id, Long userId);
}